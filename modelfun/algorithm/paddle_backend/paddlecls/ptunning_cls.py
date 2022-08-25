"""
Requirements, the Chinese name label must be equal length.
"""

import argparse
import os
import sys
import random
import time
import json
from functools import partial

import numpy as np
import paddle
import paddle.nn.functional as F

import paddlenlp as ppnlp
from paddlenlp.data import Stack, Tuple, Pad
from paddlenlp.datasets import load_dataset
from paddlenlp.transformers import LinearDecayWithWarmup

from paddlecls.ptunning.model import ErnieForPretraining, ErnieMLMCriterion
from paddlecls.ptunning.data import create_dataloader, transform_fn_dict
from paddlecls.ptunning.data import convert_example
from paddlecls.ptunning.evaluate import do_evaluate
from utils.datasets import read_data
from utils.devices import get_gpu_id


def read_fn(data_file, labeled=True):
    examples = []
    train_data = read_data(data_file, test=labeled)
    for line in train_data['json']:
        if labeled:
            example = {"sentence1": line['sentence'], "label": line['label']}
        else:
            example = {"sentence1": line['sentence']}
        yield example
        # examples.append(example)
    # return examples


# yapf: disable
# parser = argparse.ArgumentParser()

# parser.add_argument("--task_name", default='tnews', type=str, help="The task_name to be evaluated")
# parser.add_argument("--p_embedding_num", type=int, default=1, help="number of p-embedding")
# parser.add_argument("--batch_size", default=32, type=int, help="Batch size per GPU/CPU for training.")
# parser.add_argument("--learning_rate", default=1e-5, type=float, help="The initial learning rate for Adam.")
# parser.add_argument("--save_dir", default='./checkpoint', type=str, help="The output directory where the model checkpoints will be written.")
# parser.add_argument("--max_seq_length", default=128, type=int, help="The maximum total input sequence length after tokenization. "
#     "Sequences longer than this will be truncated, sequences shorter will be padded.")
# parser.add_argument("--weight_decay", default=0.0, type=float, help="Weight decay if we apply some.")
# parser.add_argument("--epochs", default=10, type=int, help="Total number of training epochs to perform.")
# parser.add_argument("--warmup_proportion", default=0.0, type=float, help="Linear warmup proption over the training process.")
# parser.add_argument("--init_from_ckpt", type=str, default=None, help="The path of checkpoint to be loaded.")
# parser.add_argument("--seed", type=int, default=1000, help="random seed for initialization")
# parser.add_argument('--device', choices=['cpu', 'gpu'], default="gpu", help="Select which device to train model, defaults to gpu.")
# parser.add_argument('--save_steps', type=int, default=10000, help="Inteval steps to save checkpoint")
# parser.add_argument("--rdrop_coef", default=0.0, type=float, help="The coefficient of KL-Divergence loss in R-Drop paper, for more detail please refer to https://arxiv.org/abs/2106.14448), if rdrop_coef > 0 then R-Drop works")

# args = parser.parse_args()
# yapf: enable
p_embedding_num=1
batch_size=32
learning_rate=1e-5
save_dir='./checkpoint'
max_seq_length = 128
weight_decay=0.0
epochs=20
warmup_proportion=0.0
# device='gpu'
save_steps=1000
rdrop_coef = 0.0

def set_seed(seed):
    """sets random seed"""
    random.seed(seed)
    np.random.seed(seed)
    paddle.seed(seed)


def ptunning_fewshot(train_path='./datasets/tnews/train.json', unlabeled_path='./datasets/tnews/train.json', val_path=None, test_path=None, num_class=2):
    # paddle.set_device(device)
    paddle.set_device('gpu:{}'.format(get_gpu_id()))

    # rank = paddle.distributed.get_rank()
    # if paddle.distributed.get_world_size() > 1:
    #     paddle.distributed.init_parallel_env()
    # set_seed(args.seed)
    print(train_path)
    train_ds = load_dataset(read_fn, data_file=train_path, lazy=False)
    val_ds = load_dataset(read_fn, data_file=val_path, lazy=False)
    test_ds = load_dataset(read_fn, data_file=test_path, lazy=False)

    # norm label 
    tmp_data = read_data(train_path, test=True)
    label_norm_dict = tmp_data['info']  #{label:label for label in labels}
    # label_norm_dict = {key: value[:2] for key, value in label_norm_dict.items()}
    # label_norm_dict[44] = '客服'
    new_label_norm_dict = {}  # build a new mapper
    min_length = 10000
    for key, value in label_norm_dict.items():
        new_label_norm_dict[key] = ''
        for v in value:  # chinese in each label
            if v > u'\u4e00' and v < u'\u9fff':
                new_label_norm_dict[key] += v
        min_length = min(len(new_label_norm_dict[key]), min_length)
    if test_path is not None:
        tmp_data = read_data(test_path, test=True)
        label_norm_dict = tmp_data['info']  #{label:label for label in labels}
        for key, value in label_norm_dict.items():
            new_label_norm_dict[key] = ''
            for v in value:  # chinese in each label
                if v > u'\u4e00' and v < u'\u9fff':
                    new_label_norm_dict[key] += v
            min_length = min(len(new_label_norm_dict[key]), min_length)

    for key, value in label_norm_dict.items():
        new_label_norm_dict[key] = new_label_norm_dict[key][:min_length]
    label_norm_dict = new_label_norm_dict
    print(label_norm_dict)
    
    convert_example_fn = convert_example
    evaluate_fn = do_evaluate
    
    # Task related transform operations, eg: numbert label -> text_label, english -> chinese
    transform_fn = partial(transform_fn_dict['custom'], label_normalize_dict=label_norm_dict, label_length=min_length)

    train_ds = train_ds.map(transform_fn, lazy=False)
    val_ds = val_ds.map(transform_fn, lazy=False)
    test_ds = test_ds.map(transform_fn, lazy=False)

    model = ErnieForPretraining.from_pretrained('ernie-3.0-base-zh')
    tokenizer = ppnlp.transformers.ErnieTokenizer.from_pretrained('ernie-3.0-base-zh')

    batchify_fn = lambda samples, fn=Tuple(
        Pad(axis=0, pad_val=tokenizer.pad_token_id),  # src_ids
        Pad(axis=0, pad_val=tokenizer.pad_token_type_id),  # token_type_ids
        Stack(dtype="int64"),  # masked_positions
        Stack(dtype="int64"),  # masked_lm_labels
    ): [data for data in fn(samples)]

    trans_func = partial(convert_example_fn,
                         tokenizer=tokenizer,
                         max_seq_length=max_seq_length,
                         p_embedding_num=p_embedding_num)
    train_data_loader = create_dataloader(train_ds,
                                          mode='train',
                                          batch_size=batch_size,
                                          batchify_fn=batchify_fn,
                                          trans_fn=trans_func)

    val_data_loader = create_dataloader(val_ds,
                                        mode='eval',
                                        batch_size=batch_size,
                                        batchify_fn=batchify_fn,
                                        trans_fn=trans_func)

    test_data_loader = create_dataloader(test_ds,
                                        mode='eval',
                                        batch_size=batch_size,
                                        batchify_fn=batchify_fn,
                                        trans_fn=trans_func)

    mlm_loss_fn = ErnieMLMCriterion()
    rdrop_loss = ppnlp.losses.RDropLoss()

    num_training_steps = len(train_data_loader) * epochs

    lr_scheduler = LinearDecayWithWarmup(learning_rate, num_training_steps, warmup_proportion)

    # Generate parameter names needed to perform weight decay.
    # All bias and LayerNorm parameters are excluded.
    decay_params = [
        p.name for n, p in model.named_parameters()
        if not any(nd in n for nd in ["bias", "norm"])
    ]
    optimizer = paddle.optimizer.AdamW(
        learning_rate=lr_scheduler,
        parameters=model.parameters(),
        weight_decay=weight_decay,
        apply_decay_param_fun=lambda x: x in decay_params)

    global_step = 0
    tic_train = time.time()
    for epoch in range(1, epochs + 1):
        model.train()
        for step, batch in enumerate(train_data_loader, start=1):

            src_ids = batch[0]
            token_type_ids = batch[1]
            masked_positions = batch[2]
            masked_lm_labels = batch[3]
            prediction_scores = model(input_ids=src_ids,
                                      token_type_ids=token_type_ids,
                                      masked_positions=masked_positions)

            if rdrop_coef > 0:
                prediction_scores_2 = model(input_ids=src_ids,
                                            token_type_ids=token_type_ids,
                                            masked_positions=masked_positions)
                ce_loss = (
                    mlm_loss_fn(prediction_scores, masked_lm_labels) +
                    mlm_loss_fn(prediction_scores_2, masked_lm_labels)) * 0.5
                kl_loss = rdrop_loss(prediction_scores, prediction_scores_2)
                loss = ce_loss + kl_loss * rdrop_coef
            else:
                loss = mlm_loss_fn(prediction_scores, masked_lm_labels)

            global_step += 1
            if global_step % 10 == 0:
                print(
                    "global step %d, epoch: %d, batch: %d, loss: %.5f, speed: %.2f step/s"
                    % (global_step, epoch, step, loss, 10 /
                       (time.time() - tic_train)))
                tic_train = time.time()
            loss.backward()
            optimizer.step()
            lr_scheduler.step()
            optimizer.clear_grad()

        # dev_accuracy, total_num = evaluate_fn(model, tokenizer, dev_data_loader,
        #                                       label_norm_dict)
        # print("epoch:{}, dev_accuracy:{:.3f}, total_num:{}".format(
        #     epoch, dev_accuracy, total_num))
        test_accuracy, total_num = evaluate_fn(model, tokenizer,
                                               test_data_loader,
                                               label_norm_dict)
        print("epoch:{}, test_accuracy:{:.3f}, total_num:{}".format(epoch, test_accuracy, total_num))
    # prepare unlabeled data.
    unlabeled_ds = load_dataset(read_fn, data_file=unlabeled_path, labeled=False, lazy=False)
    val_ds = load_dataset(read_fn, data_file=val_path, labeled=False, lazy=False)
    test_ds = load_dataset(read_fn, data_file=test_path, labeled=False, lazy=False)

    transform_fn = partial(transform_fn_dict['custom'],
                           label_normalize_dict=label_norm_dict,
                           label_length=min_length,
                           is_test=True)
    unlabeled_ds = unlabeled_ds.map(transform_fn, lazy=False)
    val_ds = val_ds.map(transform_fn, lazy=False)
    test_ds = test_ds.map(transform_fn, lazy=False)

    batchify_fn = lambda samples, fn=Tuple(
            Pad(axis=0, pad_val=tokenizer.pad_token_id),  # src_ids
            Pad(axis=0, pad_val=tokenizer.pad_token_type_id),  # token_type_ids
            Stack(dtype="int64"),  # masked_positions
        ): [data for data in fn(samples)]

    trans_func = partial(convert_example_fn,
                         tokenizer=tokenizer,
                         max_seq_length=max_seq_length,
                         p_embedding_num=p_embedding_num,
                         is_test=True)

    unlabeled_data_loader = create_dataloader(unlabeled_ds,
                                              mode='eval',
                                              batch_size=batch_size,
                                              batchify_fn=batchify_fn,
                                              trans_fn=trans_func)
    val_data_loader = create_dataloader(val_ds,
                                        mode='eval',
                                        batch_size=batch_size,
                                        batchify_fn=batchify_fn,
                                        trans_fn=trans_func)
    test_data_loader = create_dataloader(test_ds,
                                        mode='eval',
                                        batch_size=batch_size,
                                        batchify_fn=batchify_fn,
                                        trans_fn=trans_func)
    res = {}
    res['unlabeled_predictions'] = do_predict(model, tokenizer, unlabeled_data_loader, label_norm_dict, num_class)
    res['val_predictions'] = do_predict(model, tokenizer, val_data_loader, label_norm_dict, num_class)
    res['test_predictions'] = do_predict(model, tokenizer, test_data_loader, label_norm_dict, num_class)
    # label is in string format.
    return res


@paddle.no_grad()
def do_predict(model, tokenizer, data_loader, label_normalize_dict, num_class):
    model.eval()

    normed_labels = [
        normalized_lable
        for origin_lable, normalized_lable in label_normalize_dict.items()
    ]

    origin_labels = [
        origin_lable
        for origin_lable, normalized_lable in label_normalize_dict.items()
    ]

    label_length = len(normed_labels[0])

    y_pred_labels = []

    for batch in data_loader:
        src_ids, token_type_ids, masked_positions = batch

        # [bs * label_length, vocab_size]
        prediction_probs = model.predict(input_ids=src_ids,
                                         token_type_ids=token_type_ids,
                                         masked_positions=masked_positions)

        batch_size = len(src_ids)
        vocab_size = prediction_probs.shape[1]

        # prediction_probs: [batch_size, label_lenght, vocab_size]
        prediction_probs = paddle.reshape(prediction_probs,
                                          shape=[batch_size, -1,
                                                 vocab_size]).numpy()

        # [label_num, label_length]
        label_ids = np.array(
            [tokenizer(label)["input_ids"][1:-1] for label in normed_labels])

        y_pred = np.ones(shape=[batch_size, len(label_ids)])

        # Calculate joint distribution of candidate labels
        for index in range(label_length):
            y_pred *= prediction_probs[:, index, label_ids[:, index]]

        # Get max probs label's index
        y_pred_index = np.argmax(y_pred, axis=-1)
        max_prob = np.max(y_pred, axis=-1)
        
        for idx, index in enumerate(y_pred_index):
            if max_prob[idx] < 1.0/num_class:
                y_pred_labels.append(-1)
            else:   
                y_pred_labels.append(int(origin_labels[index]))

    return y_pred_labels
