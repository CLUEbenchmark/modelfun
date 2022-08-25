from paddlenlp.metrics import SpanEvaluator
import seqeval.metrics as seq_metric
from seqeval.scheme import IOB2
from typing import List
import numpy as np
from bidict import bidict
from collections import Counter


def metric(pred_span, end_span, datas):
    metric = SpanEvaluator()
    for data in datas:
        pass
        # num_correct, num_infer, num_label = metric.compute(start_prob, end_prob,
        #                                                     start_ids, end_ids)
        # metric.update(num_correct, num_infer, num_label)
    precision, recall, f1 = metric.accumulate()


def entity_to_bio_labels(entities: List[str]):
    bio_labels = ["O"] + ["%s-%s" % (bi, label) for label in entities for bi in "BI"]
    return bio_labels


def get_all_metrics(true_entity_span, pred_entity_span, original_texts, schemas):
    true_labels_all = []
    pred_labels_all = []
    for idx, text in enumerate(original_texts):
        true_labels = ['O'] * len(text)
        # print(true_entity_span[idx])
        for entity in true_entity_span[idx]:
            print(len(text), entity)
            true_labels[entity['start_offset']] = 'B-' + entity['label']
            for i in range(entity['start_offset']+1, entity['end_offset']):
                true_labels[i] = 'I-' + entity['label']
        
        pred_labels = ['O'] * len(text)
        for entity in pred_entity_span[idx]['entities']:
            # print(text, entity)
            if len(entity) == 0:
                continue
            if entity['start_offset'] > len(text) or entity['end_offset'] > len(text):
                continue
            pred_labels[entity['start_offset']] = 'B-' + entity['label']
            for i in range(entity['start_offset']+1, entity['end_offset']):
                pred_labels[i] = 'I-' + entity['label']
        true_labels_all.append(true_labels)
        pred_labels_all.append(pred_labels)
    
    # id2label =  {i: label for i, label in enumerate(schemas)}
    # metric = SeqEntityScore(id2label, markup='bio')
    # metric.update(true_labels_all, pred_labels_all)
    # res = metric.result()[0]
    # return 0, res['precision'], res['recall'], res['f1']

    accuracy = accuracy_seq(true_labels_all, pred_labels_all, strict=False)
    precision = precision_seq(true_labels_all, pred_labels_all, strict=False)
    recall = recall_seq(true_labels_all, pred_labels_all, strict=False)
    f1 = f1_score_seq(true_labels_all, pred_labels_all, strict=False)
    report = classification_report(true_labels_all, pred_labels_all, strict=False, output_dict=True)
    report = report_to_value(report)
    return accuracy, precision, recall, f1, report


def report_to_value(report):
    """
        convert value in report from numpy to list
    """
    new_report = {}
    for key, value in report.items():
        if isinstance(value, dict):
            sub = {}
            for key2, value2 in value.items():
                # print(type(value2))
                sub[key2] = value2.item()
                # print('after', type(sub[key2]))
            new_report[key] = sub
        else:
            # print(type(value))
            new_report[key] = value.item()
            # print('after', type(new_report[key]))
    return new_report


def f1_score_seq(y_true: List[List], y_pred: List[List], strict=True):
    if strict:
        return seq_metric.f1_score(y_true, y_pred, mode='strict', scheme=IOB2)
    else:
        return seq_metric.f1_score(y_true, y_pred)


def precision_seq(y_true: List[List], y_pred: List[List], strict=True):
    if strict:
        return seq_metric.precision_score(y_true, y_pred, mode='strict', scheme=IOB2)
    else:
        return seq_metric.precision_score(y_true, y_pred)


def recall_seq(y_true: List[List], y_pred: List[List], strict=True):
    if strict:
        return seq_metric.recall_score(y_true, y_pred, mode='strict', scheme=IOB2)
    else:
        return seq_metric.recall_score(y_true, y_pred)


def accuracy_seq(y_true: List[List], y_pred: List[List], strict=True):
    return seq_metric.accuracy_score(y_true, y_pred)


def classification_report(y_true: List[List], y_pred: List[List], strict=True, output_dict=True):
    if strict:
        return seq_metric.classification_report(y_true, y_pred, mode='strict', scheme=IOB2, output_dict=output_dict)
    else:
        return seq_metric.classification_report(y_true, y_pred, output_dict=output_dict)


def get_entity_bios(seq,id2label):
    """Gets entities from sequence.
    note: BIOS
    Args:
        seq (list): sequence of labels.
    Returns:
        list: list of (chunk_type, chunk_start, chunk_end).
    Example:
        # >>> seq = ['B-PER', 'I-PER', 'O', 'S-LOC']
        # >>> get_entity_bios(seq)
        [['PER', 0,1], ['LOC', 3, 3]]
    """
    chunks = []
    chunk = [-1, -1, -1]
    for indx, tag in enumerate(seq):
        if not isinstance(tag, str):
            tag = id2label[tag]
        if tag.startswith("S-"):
            if chunk[2] != -1:
                chunks.append(chunk)
            chunk = [-1, -1, -1]
            chunk[1] = indx
            chunk[2] = indx
            chunk[0] = tag.split('-')[1]
            chunks.append(chunk)
            chunk = (-1, -1, -1)
        if tag.startswith("B-"):
            if chunk[2] != -1:
                chunks.append(chunk)
            chunk = [-1, -1, -1]
            chunk[1] = indx
            chunk[0] = tag.split('-')[1]
        elif tag.startswith('I-') and chunk[1] != -1:
            _type = tag.split('-')[1]
            if _type == chunk[0]:
                chunk[2] = indx
            if indx == len(seq) - 1:
                chunks.append(chunk)
        else:
            if chunk[2] != -1:
                chunks.append(chunk)
            chunk = [-1, -1, -1]
    return chunks


def get_entity_bio(seq,id2label):  # TODO close set
    """Gets entities from sequence.
    note: BIO
    Args:
        seq (list): sequence of labels.
    Returns:
        list: list of (chunk_type, chunk_start, chunk_end).
    Example:
        seq = ['B-PER', 'I-PER', 'O', 'B-LOC']
        get_entity_bio(seq)
        #output
        [['PER', 0,1], ['LOC', 3, 3]]
    """
    chunks = []
    chunk = [-1, -1, -1]
    for indx, tag in enumerate(seq):
        if not isinstance(tag, str):
            tag = id2label[tag]
        if tag.startswith("B-"):
            if chunk[2] != -1:
                chunks.append(chunk)
            chunk = [-1, -1, -1]
            chunk[1] = indx
            chunk[0] = tag.split('-')[1]
            chunk[2] = indx
            if indx == len(seq) - 1:
                chunks.append(chunk)
        elif tag.startswith('I-') and chunk[1] != -1:
            _type = tag.split('-')[1]
            if _type == chunk[0]:
                chunk[2] = indx

            if indx == len(seq) - 1:
                chunks.append(chunk)
        else:
            if chunk[2] != -1:
                chunks.append(chunk)
            chunk = [-1, -1, -1]
    return chunks


def get_entities(seq,id2label,markup='bios'):
    '''
    :param seq:
    :param id2label:
    :param markup:
    :return:
    '''
    assert markup in ['bio','bios']
    if markup =='bio':
        return get_entity_bio(seq,id2label)
    else:
        return get_entity_bios(seq,id2label)


class SeqEntityScore(object):
    def __init__(self, id2label, markup='bios'):
        self.id2label = id2label
        self.markup = markup
        self.reset()

    def reset(self):
        self.origins = []
        self.founds = []
        self.rights = []

    def compute(self, origin, found, right):
        recall = 0 if origin == 0 else (right / origin)
        precision = 0 if found == 0 else (right / found)
        f1 = 0. if recall + precision == 0 else (2 * precision * recall) / (precision + recall)
        return recall, precision, f1

    def result(self):
        class_info = {}
        origin_counter = Counter([x[0] for x in self.origins])
        found_counter = Counter([x[0] for x in self.founds])
        right_counter = Counter([x[0] for x in self.rights])
        for type_, count in origin_counter.items():
            origin = count
            found = found_counter.get(type_, 0)
            right = right_counter.get(type_, 0)
            recall, precision, f1 = self.compute(origin, found, right)
            class_info[type_] = {"precision": round(precision, 4), 'recall': round(recall, 4), 'f1': round(f1, 4)}
        origin = len(self.origins)
        found = len(self.founds)
        right = len(self.rights)
        recall, precision, f1 = self.compute(origin, found, right)
        return {'precision': precision, 'recall': recall, 'f1': f1}, class_info

    def update(self, label_paths, pred_paths):
        '''
        labels_paths: [[],[],[],....]
        pred_paths: [[],[],[],.....]
        :param label_paths:
        :param pred_paths:
        :return:
        Example:
            >>> labels_paths = [['O', 'O', 'O', 'B-MISC', 'I-MISC', 'I-MISC', 'O'], ['B-PER', 'I-PER', 'O']]
            >>> pred_paths = [['O', 'O', 'B-MISC', 'I-MISC', 'I-MISC', 'I-MISC', 'O'], ['B-PER', 'I-PER', 'O']]
        '''
        for label_path, pre_path in zip(label_paths, pred_paths):
            label_entities = get_entities(label_path, self.id2label,self.markup)
            pre_entities = get_entities(pre_path, self.id2label,self.markup)
            self.origins.extend(label_entities)
            self.founds.extend(pre_entities)
            self.rights.extend([pre_entity for pre_entity in pre_entities if pre_entity in label_entities])


if __name__ == '__main__':
    y_true = [['O', 'O', 'O', 'B-MISC', 'I-MISC', 'I-MISC', 'O'], ['B-PER', 'I-PER', 'O']]
    y_pred = [['O', 'O', 'B-MISC', 'B-MISC', 'B-MISC', 'I-MISC', 'O'], ['B-PER', 'I-PER', 'O']]
    label_list = ['MISC', 'PER']
    id2label =  {i: label for i, label in enumerate(label_list)}
    metric = SeqEntityScore(id2label, markup='bio')
    metric.update(y_true, y_pred)
    print(metric.result())
    # assert False

    print(accuracy_seq(y_true, y_pred, strict=False))
    print(precision_seq(y_true, y_pred, strict=False))
    print(recall_seq(y_true, y_pred, strict=False))
    print(f1_score_seq(y_true, y_pred, strict=False))
    print(classification_report(y_true, y_pred, strict=True))

    # np_map = np.vectorize(lambda lb: label_to_id[lb])
    # label_list.append(np_map(data['a']))
    # print(label_list)
