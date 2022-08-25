from typing import Dict, Union, List
import numpy as np
from requests import post
import torch
import os
import random
import time
import urllib
import time
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import f1_score, recall_score, precision_score, accuracy_score, classification_report, confusion_matrix
from modelfun.utils.core import probs_to_preds
from sklearn.feature_extraction.text import CountVectorizer
from modelfun.labeling.model.label_model import LabelModel
from modelfun.utils.dataset import read_data, extract_text_label, filter_unlabeled_list
from modelfun.utils.save import save2oss, get_list_from_oss
from modelfun.utils.nlp import tokenize, get_stopword_list
from modelfun.utils.dataset import generate_mapping, preprocess_label_matrix, postprocess_label


class LogisitcModel:
    def __init__(self) -> None:
        """ Each model contains vectorizer and model
        """
        self.vectorizer = CountVectorizer(
            tokenizer=tokenize, ngram_range=(1, 5),
            stop_words=get_stopword_list()
        )
        self.sklearn_model = LogisticRegression(C=1e3, solver="liblinear")
        # self.sklearn_model = RandomForestClassifier()

    def fit(self, X, y) -> None:
        """ Fit with raw text and label
            Params: 
                X: feature matrix.
                y: label vector.
        """
        X_train = self.vectorizer.fit_transform(X)
        self.sklearn_model.fit(X_train, y)

    def score(self, X, y) -> float:
        """ Measure the score
            Parameters:
                X: feature matrix.
                y: label vector.
        """
        pred = self.predict(X)
        # add filter to fileter unlabeled classes. to be delete in the future
        # print('before filtering', len(pred))
        # unique_labels = np.unique(pred)
        # idxs = [idx for idx, i in enumerate(y) if i in unique_labels]
        # y, pred = [y[i] for i in idxs], pred[idxs]
        # print('after filtering', len(pred))
        # end adding
        acc = accuracy_score(y, pred)
        precision = precision_score(y, pred, average='weighted')
        recall = recall_score(y, pred, average='weighted')
        f1 = f1_score(y, pred, average='weighted')
        report = classification_report(y, pred, output_dict=True)
        return acc, precision, recall, f1, report

    def predict(self, X) -> np.array:
        """ Get the prediction results
            Parameters:
                X: feature matrix.
        """
        X = self.vectorizer.transform(X)
        pred = self.sklearn_model.predict(X)
        return pred

    def predict_proba(self, X) -> np.array:
        """ Get the prediction results
            Parameters:
                X: feature matrix.
        """
        X = self.vectorizer.transform(X)
        pred = self.sklearn_model.predict_proba(X)
        return pred

    def save(self, model_path: str) -> None:
        """Save the model to the specified file path
            Parameters:
                model_path: path in string format.
        """
        torch.save([self.vectorizer, self.sklearn_model], model_path)
        save2oss(model_path, model_path.split('/')[-1])
        os.remove(model_path)
        return model_path.split('/')[-1]

    def load(self, model_path: str) -> None:
        """Load a saved model
            Parameters:
                model_path: path in string format.
        """
        tmp_file_name  = '{}{}local_tempfile'.format(time.time(), random.random())
        urllib.request.urlretrieve(
            model_path, tmp_file_name)
        self.vectorizer, self.sklearn_model = torch.load(tmp_file_name)
        os.remove(tmp_file_name)


def train_logistc(train_filtered, preds_train_filtered, test_text, test_label, oss: bool=True):
    """Train a logistic regression model.
        
    Parameters
    ----------
    train_filtered
        Training set.
    preds_train_filtered:
        Predict label for training set.
    test_text: 
        Test text.
    test_label: 
        Test labels.

    Returns
    -------
    Dict
        Result dict.
    """
    model = LogisitcModel()
    model.fit(X=train_filtered, y=preds_train_filtered)
    acc, precision, recall, f1, report = model.score(test_text, test_label)
    pred = model.predict(test_text).tolist()
    confusion_mx = confusion_matrix(test_label, pred).tolist()
    del report['accuracy']
    if oss:
        ret_model = model.save('logistic-{}-{}.pt'.format(time.time(), random.random()))
    else:
        ret_model = model
    if isinstance(pred, np.ndarray):
        pred = pred.tolist()
    return {'accuracy': acc, 'precision': precision, 'recall': recall,
            'f1': f1, 'report': report, 'preds': pred, 'url': ret_model,
            'confusion_mx': confusion_mx}


def logistic_cls_from_label(train_path: Union[str, List], test_path: str, 
                            train_label: Union[List, str], labeled_path: Union[str, List], label_model_predictions: Union[List, str]=None,
                            oss: bool=True) -> Dict:
    """Training from label vector.

    Parameters
    ----------
    train_path
        path of train file or training data list

    Returns
    -------
    Dict
        Result dict.
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
    return train_logistc(train_filtered+labeled_text, preds_train_filtered.tolist()+labeled_label, test_text, test_label, oss)


def logistic_cls(train_path: str, test_path: str, train_label_matrix: Union[List, str], 
                 num_class: int, oss: bool=True) -> Dict:
    """training from label matrix.

    Parameters
    ----------
    ds
        Dataset utility.

    Returns
    -------
    Dict
        Result dict.
    """
    train_data = read_data(train_path)
    train_text, _ = extract_text_label(train_data)
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
    test_data = read_data(test_path)
    test_text, test_label = extract_text_label(test_data, train=False)

    preds_train_filtered = probs_to_preds(probs=probs_train_filtered)
    # inverse mapping
    preds_train_filtered = postprocess_label(preds_train_filtered, mapping)
    return train_logistc(train_filtered, preds_train_filtered, test_text, test_label, oss)
