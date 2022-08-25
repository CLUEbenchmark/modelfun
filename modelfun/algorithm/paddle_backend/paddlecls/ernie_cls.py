import os
import sys

sys.path.append('.')
sys.path.append('../')

import random
import time
import math
import json
from functools import partial
import shutil
import numpy as np
import paddle
from paddle.io import DataLoader
import paddle.nn as nn
from paddle.metric import Accuracy

from paddlenlp.datasets import load_dataset
from paddlenlp.data import DataCollatorWithPadding
from paddlenlp.transformers import LinearDecayWithWarmup
from paddlenlp.transformers import AutoModelForSequenceClassification, AutoTokenizer

from utils.datasets import generate_mapping_list, read_data, extract_text_label, preprocess_labels, postprocess_label
from utils.save import get_list_from_oss
from utils.devices import get_gpu_id
def set_seed(args):
    # Use the same data seed(for data shuffle) for all procs to guarantee data
    # consistency after sharding.
    random.seed(args.seed)
    np.random.seed(args.seed)
    # Maybe different op seeds(for dropout) for different procs is better. By:
    # `paddle.seed(args.seed + paddle.distributed.get_rank())`
    paddle.seed(args.seed)


def load_custom_dataset(data_path, label_list=None, label_map=None, labeled=True):
    rng = []  # label list
    if label_list is not None:
        labeled = False
    def _read(ds):
        tmp_data = read_data(ds, test=labeled)
        for line_idx, line in enumerate(tmp_data['json']):
            each = line
            if label_list is not None:  # TODO this do not accept empty line
                each['label'] = label_list[line_idx]
            each['label'] = label_map[int(each['label'])]
            if each['label'] not in rng:
                rng.append(each['label'])
            yield {'sentence': each['sentence'], 'label': each['label']}

    map_ds = load_dataset(_read, ds=data_path, lazy=False)
    lslist = list(label_map.values())
    lslist.remove(-1)
    print(lslist)
    map_ds.label_list = lslist
    return map_ds


def convert_example(example,
                    tokenizer,
                    label_list,
                    is_test=False,
                    max_seq_length=512):
    """convert a glue example into necessary features"""
    if not is_test:        ############################################
        # `label_list == None` is for regression task
        label_dtype = "int64" if label_list else "float32"
        # Get the label
        label = np.array(example["label"], dtype="int64")
    if 'sentence' in example:
        example = tokenizer(example['sentence'], max_seq_len=max_seq_length)
    elif 'sentence1' in example:
        example = tokenizer(
            example['sentence1'],
            text_pair=example['sentence2'],
            max_seq_len=max_seq_length)
    if not is_test:
        example["labels"] = label
    return example


def update_model_dropout(model, p=0.0):
    model.base_model.embeddings.dropout.p = p
    for i in range(len(model.base_model.encoder.layers)):
        model.base_model.encoder.layers[i].dropout.p = p
        model.base_model.encoder.layers[i].dropout1.p = p
        model.base_model.encoder.layers[i].dropout2.p = p


@paddle.no_grad()
def evaluate(model, loss_fct, metric, data_loader):
    model.eval()
    metric.reset()
    for batch in data_loader:
        labels = batch.pop("labels")
        logits = model(**batch)
        loss = loss_fct(logits, labels)
        correct = metric.compute(logits, labels)
        metric.update(correct)
    res = metric.accumulate()
    print("eval loss: %f, acc: %s, " % (loss.numpy(), res), end='')
    model.train()
    return res

    
def erine_classification(train_data_path, test_data_path, train_label_list=None, labeled_path=None, num_class=0, dropout=0.1,
    batch_size=32, gradient_accumulation_steps=1, max_steps=-1, max_grad_norm=1.0, adam_epsilon=1e-6, warmup_proportion=0.1,warmup_steps=0, weight_decay=0.0, save_steps=100,
    logging_steps=100, num_train_epochs=30, learning_rate=1e-4, max_seq_length=128, output_dir="./tmp/best_clue_model",
    model_name_or_path='ernie-3.0-base-zh'):
    """
        If train label in the file, then `train_label_list` is None.
    """
    output_dir="./tmp/{}{}erinecls".format(random.random(), time.time())
    assert batch_size % gradient_accumulation_steps == 0, \
        "Please make sure argmument `batch_size` must be divisible by `gradient_accumulation_steps`."
    device_str = 'gpu:{}'.format(get_gpu_id())
    paddle.set_device(device_str)
    # if paddle.distributed.get_world_size() > 1:
        # paddle.distributed.init_parallel_env()

    batch_size = int(batch_size / gradient_accumulation_steps)
    if train_data_path.startswith('http'):
        pass

    # if train_label_list is None:
    tmp_train_data = read_data(labeled_path)  # the labeled data have full label set.
    _, tmp_label_list = extract_text_label(tmp_train_data, train=False)
    print(tmp_label_list)  # what if train_label_list is not complete
    mapping = generate_mapping_list(tmp_label_list)
    print(mapping)
    if isinstance(train_label_list, str):
        train_label_list = get_list_from_oss(train_label_list)
    train_ds = load_custom_dataset(data_path=train_data_path, label_list=train_label_list, label_map=mapping)
    dev_ds = load_custom_dataset(data_path=test_data_path, label_map=mapping)
    tokenizer = AutoTokenizer.from_pretrained(model_name_or_path)

    trans_func = partial(
        convert_example,
        label_list=train_ds.label_list,
        tokenizer=tokenizer,
        max_seq_length=max_seq_length)

    train_ds = train_ds.map(trans_func, lazy=True)

    train_batch_sampler = paddle.io.BatchSampler(
        train_ds, batch_size=batch_size, shuffle=True)

    dev_ds = dev_ds.map(trans_func, lazy=True)
    dev_batch_sampler = paddle.io.BatchSampler(
        dev_ds, batch_size=batch_size, shuffle=False)

    batchify_fn = DataCollatorWithPadding(tokenizer)

    train_data_loader = DataLoader(
        dataset=train_ds,
        batch_sampler=train_batch_sampler,
        collate_fn=batchify_fn,
        num_workers=0,
        return_list=True)
    dev_data_loader = DataLoader(
        dataset=dev_ds,
        batch_sampler=dev_batch_sampler,
        collate_fn=batchify_fn,
        num_workers=0,
        return_list=True)

    num_classes = num_class  #1 if train_ds.label_list == None else len(train_ds.label_list)
    model = AutoModelForSequenceClassification.from_pretrained(
        model_name_or_path, num_classes=num_classes, device=device_str)

    if dropout != 0.1:
        update_model_dropout(model, dropout)

    # if paddle.distributed.get_world_size() > 1:
    #     model = paddle.DataParallel(model)

    if max_steps > 0:
        num_training_steps = max_steps / gradient_accumulation_steps
        num_train_epochs = math.ceil(num_training_steps /
                                     len(train_data_loader))
    else:
        num_training_steps = len(
            train_data_loader
        ) * num_train_epochs / gradient_accumulation_steps
        num_train_epochs = num_train_epochs

    warmup = warmup_steps if warmup_steps > 0 else warmup_proportion

    lr_scheduler = LinearDecayWithWarmup(learning_rate, num_training_steps,
                                         warmup)

    # Generate parameter names needed to perform weight decay.
    # All bias and LayerNorm parameters are excluded.
    decay_params = [
        p.name for n, p in model.named_parameters()
        if not any(nd in n for nd in ["bias", "norm"])
    ]
    optimizer = paddle.optimizer.AdamW(
        learning_rate=lr_scheduler,
        beta1=0.9,
        beta2=0.999,
        epsilon=adam_epsilon,
        parameters=model.parameters(),
        weight_decay=weight_decay,
        apply_decay_param_fun=lambda x: x in decay_params,
        grad_clip=nn.ClipGradByGlobalNorm(max_grad_norm))

    loss_fct = paddle.nn.loss.CrossEntropyLoss(
    ) if train_ds.label_list else paddle.nn.loss.MSELoss()

    metric = Accuracy()
    best_acc = 0.0
    global_step = 0
    tic_train = time.time()
    for epoch in range(num_train_epochs):
        for step, batch in enumerate(train_data_loader):
            labels = batch.pop("labels")#.astype(paddle.float32)
            logits = model(**batch)
            # print(logits.dtype, labels.dtype)
            loss = loss_fct(logits, labels)
            if gradient_accumulation_steps > 1:
                loss = loss / gradient_accumulation_steps
            loss.backward()
            if (step + 1) % gradient_accumulation_steps == 0:
                global_step += 1
                optimizer.step()
                lr_scheduler.step()
                optimizer.clear_grad()
                if global_step % logging_steps == 0:
                    print(
                        "global step %d/%d, epoch: %d, batch: %d, rank_id: %s, loss: %f, lr: %.10f, speed: %.4f step/s"
                        % (global_step, num_training_steps, epoch, step,
                           paddle.distributed.get_rank(), loss,
                           optimizer.get_lr(),
                           logging_steps / (time.time() - tic_train)))
                    tic_train = time.time()
                if global_step % save_steps == 0 or global_step == num_training_steps:
                    tic_eval = time.time()
                    acc = evaluate(model, loss_fct, metric, dev_data_loader)
                    print("eval done total : %s s" % (time.time() - tic_eval))
                    if acc > best_acc:
                        best_acc = acc
                        output_dir = output_dir
                        if not os.path.exists(output_dir):
                            os.makedirs(output_dir)
                        # Need better way to get inner model of DataParallel
                        model_to_save = model._layers if isinstance(
                            model, paddle.DataParallel) else model
                        model_to_save.save_pretrained(output_dir)
                        tokenizer.save_pretrained(output_dir)
                if global_step >= num_training_steps:
                    print("best_acc: ", best_acc)
                    break
                    # return {'acc': best_acc}
    print("best_acc: ", best_acc)

    # predict in test
    test_ds = load_custom_dataset(test_data_path, label_map=mapping)
    trans_func = partial(
                        convert_example,
                        label_list=train_ds.label_list,
                        tokenizer=tokenizer,
                        max_seq_length=max_seq_length)
    test_ds = test_ds.map(trans_func, lazy=True)
    test_batch_sampler = paddle.io.BatchSampler(test_ds, batch_size=128, shuffle=False)
    test_data_loader = DataLoader(
        dataset=test_ds,
        batch_sampler=test_batch_sampler,
        collate_fn=batchify_fn,
        num_workers=0,
        return_list=True)

    # num_classes = 1 if train_ds.label_list == None else len(train_ds.label_list)

    model = AutoModelForSequenceClassification.from_pretrained(
        output_dir, num_classes=num_classes)

    res_predict = []
    res_true = []

    for step, batch in enumerate(test_data_loader):
        with paddle.no_grad():
            true_labels = batch.pop("labels")
            for i in true_labels:
                res_true.append(test_ds.label_list[i.item()])  # train ds may missing label
            logits = model(**batch)
        preds = paddle.argmax(logits, axis=1)
        for idx, pred in enumerate(preds):
            # j = json.dumps({"id": idx, "label": train_ds.label_list[pred]})
            res_predict.append(test_ds.label_list[pred.item()])
        print('batch', res_true, res_predict)
    res_predict = postprocess_label(res_predict, mapping)
    res_true = postprocess_label(res_true, mapping)
    shutil.rmtree(output_dir)
    return {'acc': best_acc, 'true': res_true, 'predict': res_predict}


if __name__ == "__main__":
    erine_classification(
        # '/home/modelfun/alg/modelfun-algo/datasets/OPPO-7类别包含训练集0629/unlabeled_data2.json',
    '/home/modelfun/alg/modelfun-algo/datasets/OPPO-7类别包含训练集0629/traindata2.json',
    '/home/modelfun/alg/modelfun-algo/datasets/OPPO-7类别包含训练集0629/testdata2.json',
    num_train_epochs=300)
