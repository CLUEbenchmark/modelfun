"""
    wrapper for classification model.
    If needed?
"""
import numpy as np
from typing import Union, List
from sklearn.metrics import f1_score, recall_score, precision_score, accuracy_score, classification_report, confusion_matrix
import time
import random


def postprocess_erine(true_label: List,
                      predict_label: List,
                      label_model_predictions: Union[List, str]):
    acc = accuracy_score(true_label, predict_label)
    precision = precision_score(true_label, predict_label, average='weighted')
    recall = recall_score(true_label, predict_label, average='weighted')
    f1 = f1_score(true_label, predict_label, average='weighted')
    report = classification_report(true_label, predict_label, output_dict=True)    
    confusion_mx = confusion_matrix(true_label, predict_label).tolist()
    del report['accuracy']
    ret_model = ''  # TODO update the path
    if isinstance(predict_label, np.ndarray):
        predict_label = predict_label.tolist()
    return {'accuracy': acc, 'precision': precision, 'recall': recall,
            'f1': f1, 'report': report, 'preds': predict_label, 'url': ret_model,
            'confusion_mx': confusion_mx}
