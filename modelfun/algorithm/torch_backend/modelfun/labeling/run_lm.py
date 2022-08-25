import numpy as np
from typing import List, Tuple, Dict, Optional, Union
import os
import sklearn
import random
import time
from sklearn.preprocessing import LabelEncoder
import torch
from collections import defaultdict
from modelfun.labeling.model.label_model import LabelModel
from modelfun.utils.dataset import read_data, extract_text_label
from modelfun.utils.dataset import generate_mapping, preprocess_label_matrix, postprocess_label
from modelfun.utils.save import save2oss, getfromoss, save_list_to_oss, get_list_from_oss
from modelfun.utils.confidence import calculate_score_cls


def train_lm(train_label_matrix: Union[str, List], num_class:int, soft: bool = False, oss: bool = True) -> Dict:
    """
        Train a labeling model based on the traning labeling matrix.
        Params:
            train_label_matrix: Training matrix
            num_class: Number of classes for this task.
            soft: Return soft label or not. (default False, discrete label)
            oss: Use oss or not. (default True, upload data to oss)
        Return:
            in value or the path.
    """
    if isinstance(train_label_matrix, str):  # if is a path
        train_label_matrix = get_list_from_oss(train_label_matrix)
    mapping = generate_mapping(train_label_matrix)
    train_label_matrix = preprocess_label_matrix(train_label_matrix, mapping)
    L = np.array(train_label_matrix)
    label_model = LabelModel(cardinality=num_class, device='cuda:0')
    label_model.fit(L)
    if soft:  # TODO soft not support mapping
        train_res =  label_model.predict_proba(L).tolist()
    else:
        train_res = label_model.predict(L).tolist()
        train_res = postprocess_label(train_res, mapping)
    prefix = '{}{}'.format(time.time(), random.random())
    # save model to oss
    if oss:
        label_model_path = prefix + 'label_model.pkl'
        label_model.save(label_model_path)
        save2oss(label_model_path, label_model_path)
        os.remove(label_model_path)
        # save mapping to oss
        mapping_model_path = prefix + 'mapping_model.pkl'
        torch.save(mapping, mapping_model_path)
        save2oss(mapping_model_path, mapping_model_path)
        os.remove(mapping_model_path)
        return {'label_model_path': label_model_path, 
                'mapping_model_path': mapping_model_path}  # , 'train_labe': train_res
    else:
        return label_model, mapping, train_res
        

# def lm_predict(ds: LabelModelInput, soft: bool = False) -> Dict:
def lm_predict(label_model_path: Union[str, LabelModel], 
               mapping_model_path: Union[str, LabelModel], 
               num_class: int, 
               train_label_matrix: Union[str, List], 
               val_label_matrix: Union[str, List], 
               test_label_matrix: Union[str, List], 
               test_path: str,
               soft: bool = False,
               oss: bool = False) -> Dict:
    """
        Predict new labels based on the given labeling matrix and label model
        Params:
            train_label_matrix: label matrix for unlabeled data.

    """
    if isinstance(label_model_path, str):
        prefix  = '{}{}'.format(time.time(), random.random())
        # get model from oss
        tmp_file_name = prefix+'local_tempfile'
        getfromoss(label_model_path, tmp_file_name)
        label_model = LabelModel(cardinality=num_class, device='cuda:0')
        label_model.load(tmp_file_name)
        os.remove(tmp_file_name)
        # get mapping from oss
        getfromoss(mapping_model_path, tmp_file_name)
        mapping = torch.load(tmp_file_name)
        os.remove(tmp_file_name)
    else:
        label_model = label_model_path
        mapping = mapping_model_path
    # get test label 
    train_pred, val_pred, test_pred = [], [], []
    metrics = defaultdict(None)
    if len(test_label_matrix) > 0:
        test_data = read_data(test_path)
        _, test_label = extract_text_label(test_data, train=False)
        # mapping
        test_label_matrix = get_list_from_oss(test_label_matrix)
        test_label_matrix = preprocess_label_matrix(test_label_matrix, mapping)
        L_test = np.array(test_label_matrix)
        test_pred = label_model.predict(L_test)
        # inverse mapping. must be done before calculate metrics.
        test_pred = np.array(postprocess_label(test_pred.tolist(), mapping))
        # add filter to fileter unlabeled classes. to be delete in the future
        # unique_labels = np.unique(test_pred)
        # idxs = [idx for idx, i in enumerate(test_label) if i in unique_labels]
        # test_label, test_pred = [test_label[i] for i in idxs], test_pred[idxs]
        # end adding
        # metrics = label_model.score(L_test, Y=np.array(test_label), metrics=['accuracy', 'f1_macro'])
        metrics = {}
        metrics['accuracy'] = sklearn.metrics.accuracy_score(np.array(test_label), test_pred)
        metrics['f1_macro'] = sklearn.metrics.f1_score(np.array(test_label), test_pred,
                                                            average='macro')
        metrics['recall_marco'] = sklearn.metrics.recall_score(np.array(test_label), test_pred,
                                                            average='macro')
        metrics['precision_macro'] = sklearn.metrics.precision_score(np.array(test_label), test_pred,
                                                                    average='macro')
        test_pred = test_pred.tolist()
    
    if len(val_label_matrix) > 0:
        # mapping
        if isinstance(val_label_matrix, str):
            val_label_matrix = get_list_from_oss(val_label_matrix)
        val_label_matrix = preprocess_label_matrix(val_label_matrix, mapping)
        L_val = np.array(val_label_matrix)
        val_pred = label_model.predict(L_val).tolist()
        # inverse mapping
        val_pred = postprocess_label(val_pred, mapping)
    print(train_label_matrix)
    if len(train_label_matrix) > 0:
        # mapping
        if isinstance(train_label_matrix, str):
            train_label_matrix = get_list_from_oss(train_label_matrix)
        train_label_matrix = preprocess_label_matrix(train_label_matrix, mapping)
        L_train = np.array(train_label_matrix)
        # train_pred = label_model.predict(L_train).tolist()
        train_pred_probs = label_model.predict_proba(L_train)
        train_pred = np.argmax(train_pred_probs, axis=-1)
        ranking = calculate_score_cls(train_pred_probs)
        print(len(ranking))
        print(ranking)
        certainty_idx = ranking[:int(len(ranking)/1.1)].tolist()
        uncertainty_idx = ranking[int(len(ranking)/1.1):].tolist()
        print(certainty_idx, uncertainty_idx)
        # inverse mapping
        train_pred = postprocess_label(train_pred, mapping)
    if oss:
        suffix = '{}-{}'.format(random.random(), time.time())
        if not isinstance(train_pred, List):
            train_pred = train_pred.tolist()
        if not isinstance(val_pred, List):
            val_pred = val_pred.tolist()
        if not isinstance(test_pred, List):
            test_pred = test_pred.tolist()
        if not isinstance(certainty_idx, List):
            certainty_idx = certainty_idx.tolist()
        if not isinstance(uncertainty_idx, List):
            uncertainty_idx = uncertainty_idx.tolist()
        
        save_list_to_oss(train_pred, 'pred/train'+suffix)
        save_list_to_oss(val_pred, 'pred/val'+suffix)
        save_list_to_oss(test_pred, 'pred/test'+suffix)
        save_list_to_oss(certainty_idx, 'pred/certainty'+suffix)
        save_list_to_oss(uncertainty_idx, 'pred/uncertainty'+suffix)
        return {
                # 'train_label': train_pred,
                'train_label': 'pred/train'+suffix,
                'certainty_idx': 'pred/certainty'+suffix, 
                'uncertainty_idx': 'pred/uncertainty'+suffix, 
                'val_label': 'pred/val'+suffix,
                'test_label': 'pred/test'+suffix,
                'accuracy': metrics['accuracy'], 
                'f1': metrics['f1_macro'], 
                'recall': metrics['recall_marco'], 
                'precision': metrics['precision_macro']
                }
    else:
        return {
                # 'train_label': train_pred,
                'train_label': train_pred,
                'certainty_idx': certainty_idx,
                'uncertainty_idx': uncertainty_idx, 
                'val_label': val_pred,
                'test_label': test_pred,
                'accuracy': metrics['accuracy'], 
                'f1': metrics['f1_macro'], 
                'recall': metrics['recall_marco'], 
                'precision': metrics['precision_macro']
                }