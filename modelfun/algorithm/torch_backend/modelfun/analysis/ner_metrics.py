import seqeval.metrics as seq_metric
from seqeval.scheme import IOB2
from typing import List


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
            true_labels[int(entity['start_offset'])] = 'B-' + entity['label']
            for i in range(int(entity['start_offset'])+1, int(entity['end_offset'])):
                true_labels[i] = 'I-' + entity['label']
        
        pred_labels = ['O'] * len(text)
        for entity in pred_entity_span[idx]['entities']:
            if len(entity) == 0:
                continue
            if entity['start_offset'] >= len(text) or entity['end_offset'] >= len(text):
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
    return accuracy, precision, recall, f1, report


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


if __name__ == '__main__':
    pass
