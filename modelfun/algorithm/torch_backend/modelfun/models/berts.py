import random
import time
import numpy as np
from typing import Dict, Union, List
from sklearn.metrics import confusion_matrix, f1_score, recall_score, precision_score, accuracy_score
from modelfun.labeling.model.label_model import LabelModel
from modelfun.utils.dataset import read_data, extract_text_label, filter_unlabeled_list
from modelfun.models.classifier import Classifier
from modelfun.utils.core import probs_to_preds
from modelfun.utils.save import get_list_from_oss
from modelfun.utils.dataset import generate_mapping, preprocess_label_matrix, postprocess_label, generate_mapping_list, preprocess_labels
from transformers import pipeline

def train_bert(num_class, train_filtered, preds_train_filtered, 
               test_text, test_label, oss: bool=True, pretrain: str='hfl/rbtl3') -> Dict:
    # huggingface transformers may encounter errors while label is not countious.
    mapping = generate_mapping_list(preds_train_filtered)
    preds_train_filtered = preprocess_labels(preds_train_filtered, mapping)
    # classifer  TODO  add some requirements for input num_class
    # model = Classifier(num_class=len(np.unique(preds_train_filtered)))
    model = Classifier(num_class=num_class, pretrain_name=pretrain)

    if isinstance(train_filtered, list):
        model.fit(X=train_filtered, y=preds_train_filtered)
    else:
        model.fit(X=train_filtered.tolist(), y=preds_train_filtered)
    # acc = model.score(X=test_text, y=test_label)
    test_dataset = model.preprocessing(test_text)
    test_outputs = model.trainer.predict(test_dataset)
    pred = test_outputs.predictions.argmax(axis=-1)
    pred = postprocess_label(pred, mapping)
    acc, precision, recall, f1, report = model.score(pred, test_label)
    confusion_mx = confusion_matrix(test_label, pred).tolist()
    del report['accuracy']
    if oss:
        ret_model = model.save('bert-{}{}.pt'.format(time.time(), random.random()))
    else:
        ret_model = model
    if isinstance(pred, np.ndarray):
        pred = pred.tolist()
    return {'accuracy': acc, 'precision': precision, 'recall': recall,
            'f1': f1, 'report': report, 'preds': pred,
            'url': ret_model, 'confusion_mx': confusion_mx}


def bert_cls_from_label(train_path: Union[str, List], 
                        test_path: str, 
                        train_label: List, 
                        num_class: int, 
                        labeled_path: str,
                        label_model_predictions: Union[List, str]=None,
                        oss: bool=True, 
                        pretrain: str='hfl/rbtl3'):
    """
        training from label vector.
    """
    if isinstance(train_path, str):
        train_data = read_data(train_path)
        train_text, _ = extract_text_label(train_data)
    else:
        train_text = train_path
    # get aggregated label
    if isinstance(train_label, str):
        train_label = get_list_from_oss(train_label)
    # labeled path
    labeled_text, labeled_label = [], []
    if labeled_path is not None:
        if isinstance(labeled_path, str):
            labeled_data = read_data(labeled_path)
            labeled_text, labeled_label = extract_text_label(labeled_data, train=False)
    preds_train = np.array(train_label)
    train_filtered = [train_text[idx]
                      for idx, item in enumerate(preds_train) if item != -1]
    preds_train_filtered = preds_train[preds_train != -1]
    test_data = read_data(test_path)
    test_text, test_label = extract_text_label(test_data, train=False)
    return train_bert(num_class, train_filtered+labeled_text, preds_train_filtered.tolist()+labeled_label, test_text, test_label, oss, pretrain=pretrain)


def bert_cls(train_path: str, test_path: str, train_label_matrix: Union[List, str], num_class: int, oss: bool=True) -> Dict:
    if isinstance(train_path, str):
        train_data = read_data(train_path)
        train_text, _ = extract_text_label(train_data)
    else:
        train_text = train_path
    # mapping
    if isinstance(train_label_matrix, str):
        train_label_matrix = get_list_from_oss(train_label_matrix)
    mapping = generate_mapping(train_label_matrix)
    train_label_matrix = preprocess_label_matrix(train_label_matrix, mapping)
    # get aggregated label
    L_train = np.array(train_label_matrix)
    label_model = LabelModel(cardinality=num_class, device='cpu')
    label_model.fit(L_train)
    probs_train = label_model.predict_proba(L=L_train)

    train_filtered, probs_train_filtered = filter_unlabeled_list(
        X=train_text, y=probs_train, L=L_train
    )
    preds_train_filtered = probs_to_preds(probs=probs_train_filtered)
    # inverse mapping
    preds_train_filtered = postprocess_label(preds_train_filtered, mapping)
    test_data = read_data(test_path)
    test_text, test_label = extract_text_label(test_data, train=False)
    return train_bert(num_class, train_filtered, preds_train_filtered, test_text, test_label, oss)


def fsl_cls(data_list: List, label_text_list: List, threshold: int=-1):
    """
        training from label vector.
        data_list: ["text_1", "text_2", ...]
        label_text_list: ["label_text_1", "label_text_2", ...]
        ---
        !Example:
        
        input:
        text_list = ["保险什么时候开始?", "请问保费能退多少?"]
        label_text_list = ['投保', '退保', '咨询']
        
        return:
        [{'sequence': '保险什么时候开始?',
        'labels': ['咨询', '投保', '退保'],
        'scores': [0.6592103838920593, 0.2858297526836395, 0.05495985597372055],
        'pred': 0,
        'pred_text': '投保'},
        {'sequence': '请问保费能退多少?',
        'labels': ['咨询', '退保', '投保'],
        'scores': [0.600115180015564, 0.34086260199546814, 0.05902226269245148],
        'pred': 0,
        'pred_text': '投保'}]
    """
    classifier = pipeline("zero-shot-classification", model="MoritzLaurer/mDeBERTa-v3-base-mnli-xnli")
    results = classifier(data_list, label_text_list)
    if threshold != -1:
        results = results[results['score'] > threshold]
    for r in results:
        r["pred"] = np.array(r["scores"]).argmax()
        r["pred_text"] = label_text_list[r["pred"]]
    return results



def fsl_cls(data_list: List, label_text_list: List, label_list: List, threshold: int=-1):
    results = fsl_cls(data_list, label_text_list, threshold)
    preds = [r["pred"] for r in results]
    preds = np.array(preds)
    labels = np.array(label_list)
    acc = accuracy_score(labels, preds)
    precision = precision_score(labels, preds)
    f1 = f1_score(labels, preds)
    recall = recall_score(labels, preds)
    return {'accuracy': acc, 'precision': precision, 'recall': recall, 'f1': f1, 'preds': preds}
