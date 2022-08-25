"""
    Add fewshot model to increase the visibility.
"""
from cProfile import label
from typing import Union, List
import numpy as np
import sys
from bidict import bidict
sys.path.append('.')
sys.path.append('../')
from modelfun.utils.dataset import read_data, extract_text_label, postprocess_label
from modelfun.models.logistic import LogisitcModel
from modelfun.models.classifier import Classifier


def predict_data(model, text):
    res = model.predict_proba(text)
    pred_labels = np.argmax(res, axis=-1)
    final_predictions = []
    for idx, value in enumerate(pred_labels):
        if res[idx, value] < 0.5:
            final_predictions.append(-1)
        else:
            final_predictions.append(value.item())
    return final_predictions


def fewshot_cls(train_path: Union[str, List], unlabeled_path: str, val_path: str,
 test_path: str, num_class: int, oss: bool=True):
    """Training from label vector.

    Parameters
    ----------
    train_path
        path of train file or training data list. (have label)

    Returns
    -------
    Dict
        Result dict.
    """
    # train
    if isinstance(train_path, str):
        train_data = read_data(train_path, test=True)
        train_text, train_label = extract_text_label(train_data, train=False)
    else:
        train_text = train_path
    
    # unlabeled
    if isinstance(unlabeled_path, str):
        unlabeled_data = read_data(unlabeled_path)
        unlabeled_text, _ = extract_text_label(unlabeled_data)
    else:
        unlabeled_text = unlabeled_path

    # val
    if isinstance(val_path, str):
        val_data = read_data(val_path)
        val_text, _ = extract_text_label(val_data)
    else:
        val_text = val_path

    # test
    if isinstance(test_path, str):
        test_data = read_data(test_path)
        test_text, _ = extract_text_label(test_data)
    else:
        test_text = test_path

    print('in few shot')
    model_name = 'lr'
    final_predictions = {}
    label_map = {-1: -1}
    for i in train_label:
        if i not in label_map:
            label_map[i] = len(label_map) - 1
    label_map = bidict(label_map)

    if model_name == 'lr':
        # todo label not consistent
        model = LogisitcModel()
        train_label = [label_map[i] for i in train_label]
        model.fit(X=train_text, y=train_label)
    elif model_name == 'bert':
        model = Classifier(num_class)
        train_label = [label_map[i] for i in train_label]
        model.fit(X=train_text, y=train_label)
    else:
        raise NotImplementedError
    final_predictions['unlabeled_predictions'] = postprocess_label(predict_data(model, unlabeled_text), label_map)
    if val_text is not None:
        final_predictions['val_predictions'] = postprocess_label(predict_data(model, val_text), label_map)
    if test_text is not None:
        final_predictions['test_predictions'] = postprocess_label(predict_data(model, test_text), label_map)
    if val_text is None:
        return final_predictions['unlabeled_predictions']
    else:
        # print(final_predictions)
        return final_predictions
    

if __name__ == '__main__':
    pass
