import sys
from sklearn import metrics
from datasets import load_metric
from transformers import AutoTokenizer, BertTokenizerFast

sys.path.append('.')
sys.path.append('../')

from typing import List, Dict, Union
from modelfun.utils.dataset import read_data, extract_text_label
from modelfun.analysis.ner_metrics import get_all_metrics
from transformers import DataCollatorForTokenClassification
from transformers import AutoModelForTokenClassification, TrainingArguments, Trainer
from transformers import pipeline
import numpy as np
import pandas as pd
import shutil
import datasets
import random 
import time
import os
from modelfun.models.dataloder import CLUENERDataset
from modelfun.utils.confidence import calculate_score
from modelfun.utils.save import save2oss, archive
from modelfun.utils.devices import get_gpu_id


def train_ner(train_path: str, test_path: str, unlabeled_path: str,
              schemas: List, backbone: str='hfl/chinese-macbert-base', oss: bool=True):
    """
        Train NER model
        Args:
        train_path: Train file path.
        test_path: Test file path.
        unlabeled_path: Unlabeled file path.
        schemas: entity list.
        backbone: backbone model to use.
        tags: .
    Returns:
        Loss tensor with the reduction option applied.
    """
    # read data
    unlabeled_data = read_data(unlabeled_path, task_type='ner')
    unlabeled_texts, _ = extract_text_label(unlabeled_data, task_type='ner')
    
    train_data = read_data(train_path, test=True, task_type='ner')
    train_texts, [train_entities, train_relations] = extract_text_label(train_data, train=False, task_type='ner')
    test_data = read_data(test_path, test=True, task_type='ner')
    test_texts, [test_entities, test_relations] = extract_text_label(test_data, train=False, task_type='ner')

    def tokenize_and_align_labels(examples):
        tokenized_inputs = tokenizer(examples["tokens"], is_split_into_words=True, truncation=True, padding=True, max_length=512)
        # print(examples['tokens'])
        # print(tokenized_inputs)
        # print('return tokens', tokenized_inputs.tokens())
        # assert False
        labels = []
        for i, label in enumerate(examples[f"ner_tags"]):
            word_ids = tokenized_inputs.word_ids(batch_index=i)  # Map tokens to their respective word.
            previous_word_idx = None
            label_ids = []
            for word_idx in word_ids:  # Set the special tokens to -100.
                if word_idx is None:
                    label_ids.append(-100)
                elif word_idx != previous_word_idx:  # Only label the first token of a given word.
                    label_ids.append(-100 if word_idx is None else label[word_idx])
                else:
                    label_ids.append(-100)
                    # label_ids.append(label[word_idx])
                previous_word_idx = word_idx
            labels.append(label_ids)
        tokenized_inputs["labels"] = labels
        return tokenized_inputs

    # transform to BIO
    # bio_data_list = []
    if backbone == 'ckiplab/bert-base-chinese-ner':
        tokenizer = BertTokenizerFast.from_pretrained('bert-base-chinese')
    else:
        tokenizer = AutoTokenizer.from_pretrained(backbone)
    label_names = [f"B-{schema}" for schema in schemas] + [f"I-{schema}" for schema in schemas] + ["O"]
    print(label_names)
    id2label = {i: label for i, label in enumerate(label_names)}
    label2id = {v: k for k, v in id2label.items()}
    model = AutoModelForTokenClassification.from_pretrained(backbone, 
                                                            # num_labels=2*len(schemas)+1,
                                                            id2label=id2label,
                                                            label2id=label2id)
    assert model.config.num_labels == 2*len(schemas)+1
    hf_dataset = {}
    # train
    dataset = CLUENERDataset(backbone, train_data['json'], schemas)  
    tokens = dataset.examples["tokens"]
    ner_tags = dataset.examples["ner_tags"]
    table = pd.DataFrame({"tokens": tokens, "ner_tags": ner_tags})
    hf_dataset["train"] = datasets.arrow_dataset.Dataset(datasets.table.InMemoryTable.from_pandas(table))
    hf_dataset["train"] = hf_dataset["train"].map(tokenize_and_align_labels, batched=True)
    print('prepared train')

    dataset = CLUENERDataset(backbone, test_data['json'], schemas)
    tokens = dataset.examples["tokens"]
    ner_tags = dataset.examples["ner_tags"]
    table = pd.DataFrame({"tokens": tokens, "ner_tags": ner_tags})
    hf_dataset["test"] = datasets.arrow_dataset.Dataset(datasets.table.InMemoryTable.from_pandas(table))
    hf_dataset["test"] = hf_dataset["test"].map(tokenize_and_align_labels, batched=True)
    print('prepared test')
    
    dataset = CLUENERDataset(backbone, unlabeled_data['json'], schemas, split="unlabeled")
    tokens = dataset.examples["tokens"]
    ner_tags = dataset.examples["ner_tags"]
    table = pd.DataFrame({"tokens": tokens, "ner_tags": ner_tags})
    hf_dataset["unlabled"] = datasets.arrow_dataset.Dataset(datasets.table.InMemoryTable.from_pandas(table))
    hf_dataset["unlabled"] = hf_dataset["unlabled"].map(tokenize_and_align_labels, batched=True)
    print('prepared unlabeled')
    
    data_collator = DataCollatorForTokenClassification(tokenizer=tokenizer)

    def compute_metrics(p):
        predictions, labels = p
        predictions = np.argmax(predictions, axis=2)

        # Remove ignored index (special tokens)
        true_predictions = [
            [label_names[p] for (p, l) in zip(prediction, label) if l != -100]
            for prediction, label in zip(predictions, labels)
        ]
        true_labels = [
            [label_names[l] for (p, l) in zip(prediction, label) if l != -100]
            for prediction, label in zip(predictions, labels)
        ]

        results = metric.compute(predictions=true_predictions, references=true_labels)
        flattened_results = {
            "overall_precision": results["overall_precision"],
            "overall_recall": results["overall_recall"],
            "overall_f1": results["overall_f1"],
            "overall_accuracy": results["overall_accuracy"],
        }
        for k in results.keys():
            if(k not in flattened_results.keys()):
                flattened_results[k+"_f1"]=results[k]["f1"]

        return flattened_results

    training_args = TrainingArguments(
        output_dir="../tmp",
        evaluation_strategy="epoch",
        learning_rate=2e-4,
        per_device_train_batch_size=2,
        per_device_eval_batch_size=2,
        num_train_epochs=10,
        weight_decay=0.01,
        save_total_limit=1,
        # report_to="wandb",
    )

    metric = load_metric("seqeval")
    
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=hf_dataset["train"],
        eval_dataset=hf_dataset["test"],
        tokenizer=tokenizer,
        data_collator=data_collator,
        compute_metrics=compute_metrics
    )

    trainer.train()
    # wandb.finish()
    # save model
    model_path = '{}-{}'.format(time.time(), random.random())
    model.save_pretrained('./tmpmodel/{}model/'.format(model_path))
    archive('tmpmodel/' + model_path, './tmpmodel/{}model/'.format(model_path))
    if oss:
        save2oss('tmpmodel/' + model_path + '.zip', model_path + '.zip')
    # os.remove('./tmpmodel/{}model/'.format(model_path))
    shutil.rmtree('./tmpmodel/{}model/'.format(model_path), ignore_errors=True)

    test_res = []  # preidction of test file in doccano format.
    predictions = trainer.predict(hf_dataset["test"])
    preds = np.argmax(predictions.predictions, axis=-1)
    for i, pred in enumerate(preds):
        text = test_texts[i]  #.strip().replace("\n", '')
        cur_res = {"text": text}
        entities = []
        s, e = -1, -1
        for idx, p in enumerate(pred):
            # print(pred)
            # print(text)
            # print(idx, p, len(text), len(pred))
            if p != 0:
                if s == -1:
                    s = idx
                    label = dataset.rev_ner_tag_dict[p][2:]
                else:
                    new_label = dataset.rev_ner_tag_dict[p][2:]
                    if new_label == label:
                        pass
                    else:
                        e = idx
                        entities.append({"text": text[s-1:e-1], "label": label, "start_offset": s-1, "end_offset": e-1})
                        # entities.append({"text": text[s:e], "label": label, "start_offset": s, "end_offset": e})
                        s, e = idx, -1
                        label = new_label
            else:
                if s != -1:
                    e = idx
                    entities.append({"text": text[s-1:e-1], "label": label, "start_offset": s-1, "end_offset": e-1})
                    # entities.append({"text": text[s:e], "label": label, "start_offset": s, "end_offset": e})
                    s, e = -1, -1
            if idx > len(text) +1:
                if s != -1:
                    e = idx
                    entities.append({"text": text[s-1:e-1], "label": label, "start_offset": s-1, "end_offset": e-1})
                    # entities.append({"text": text[s:e], "label": label, "start_offset": s, "end_offset": e})
                    s, e = -1, -1
                break
        cur_res["entities"] = entities
        test_res.append(cur_res)
    
    # TODO  get confidence score.
    probabilities = []
    entities_length = []
    unlabel_res = [] # preidction of unlabeled file in doccano format.
    predictions = trainer.predict(hf_dataset["unlabled"])
    preds = np.argmax(predictions.predictions, axis=-1)
    for i, pred in enumerate(preds):
        single_prob = []
        single_len = []
        cur_res = {"text": unlabeled_texts[i]}
        entities = []
        s, e = -1, -1
        for idx, p in enumerate(pred):
            if p != 0:
                if s == -1:
                    s = idx
                    label = dataset.rev_ner_tag_dict[p][2:]
                else:
                    new_label = dataset.rev_ner_tag_dict[p][2:]
                    if new_label == label:
                        pass
                    else:
                        e = idx
                        entities.append({"text": text[s-1:e-1], "label": label, "start_offset": s-1, "end_offset": e-1})
                        # entities.append({"text": text[s:e], "label": label, "start_offset": s, "end_offset": e})
                        single_prob.append(predictions.predictions[i, idx, p])
                        single_len.append(e - s)
                        s, e = idx, -1
                        label = new_label
            else:
                if s != -1:
                    e = idx
                    entities.append({"text": text[s-1:e-1], "label": label, "start_offset": s-1, "end_offset": e-1})
                    # entities.append({"text": text[s:e], "label": label, "start_offset": s, "end_offset": e})
                    single_prob.append(predictions.predictions[i, idx, p])
                    single_len.append(e - s)
                    s, e = -1, -1
            if idx > len(text) +1:
                if s != -1:
                    e = idx
                    entities.append({"text": text[s-1:e-1], "label": label, "start_offset": s-1, "end_offset": e-1})
                    # entities.append({"text": text[s:e], "label": label, "start_offset": s, "end_offset": e})
                    single_prob.append(predictions.predictions[i, idx, p])
                    single_len.append(e - s)
                    s, e = -1, -1
                break
        cur_res["entities"] = entities
        probabilities.append(single_prob)
        entities_length.append(single_len)
        # print(pred, cur_res)
        unlabel_res.append(cur_res)
    text_length = [len(i) for i in unlabeled_texts]
    ranking = calculate_score(probabilities, entities_length, text_length, method='mix')
    certainty_idx = ranking[:len(ranking)//2]
    uncertainty_idx = ranking[len(ranking)//2:]
    # print(test_entities, test_res)
    accuracy, precision, recall, f1, report = get_all_metrics(test_entities, test_res, test_texts, schemas)
    print(accuracy, precision, recall, f1)
    report = report_to_value(report)
    # nlp = pipeline("token-classification", model=model, tokenizer=tokenizer,
    #                aggregation_strategy="first", device=get_gpu_id())
    # print(nlp('市场仍存在对网络销售形式的需求，网络购彩前景如何？为此此我们采访业内专家程阳先生。'))
    return {'test_res': test_res, 'model_path': model_path+'.zip',
        # 'unlabeled_res': unlabel_res, 
        # 'certainty_idx': certainty_idx.tolist(), 'uncertainty_idx': uncertainty_idx.tolist(), 
        'accuracy': accuracy, 
        'precision': precision, 'recall': recall, 'fscore': f1, 'report': report}


def report_to_value(report): 
    """
        convert value in report from numpy to value
    """
    new_report = {}
    for key, value in report.items():
        if isinstance(value, dict):
            sub = {}
            for key2, value2 in value.items():
                sub[key2] = value2.item()
                # print('after', type(sub[key2]))
            new_report[key] = sub
        else:
            new_report[key] = value.item()
            # print('after', type(new_report[key]))
    return new_report


def unit_test_main():
    unlabeled_path = './datasets/tmp/unlabeled_data0613.json'
    test_path = './datasets/tmp/testdata_0613_generate_generate.json'
    train_path = './datasets/tmp/traindata_0613_generate.json'
    labels = ["地址", "组织", "游戏", "景点", "书籍","姓名", "政府", "电影", "公司", "职位"]
    # train_ner(train_path=train_path, test_path=test_path, unlabeled_path=unlabeled_path, schemas=labels, oss=False)  
    train_ner(train_path="datasets/msra_ner/train.json", test_path="datasets/msra_ner/dev.json", unlabeled_path="datasets/msra_ner/dev.json", schemas = ["PER", "ORG", "LOC"],)
    # ckiplab/bert-base-chinese-ner    xxxx
    # uer/roberta-base-finetuned-cluener2020-chinese   xxx
    # hfl/chinese-electra-180g-large-generator  0.72
    # shibing624/bert4ner-base-chinese xxx
    # hfl/chinese-roberta-wwm-ext-large  0.77


if __name__ == "__main__":
    unit_test_main()

