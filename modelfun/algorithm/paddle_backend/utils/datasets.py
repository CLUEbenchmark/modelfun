import os
import json
import tempfile
import time
import numpy as np
from typing import Dict, List, Mapping, Tuple
import urllib.request
import random
from bidict import bidict, BidictBase


def read_data_local(file_path: str, test: bool = False, info: bool=True, task_type: str='classification') -> Dict:
    """
        read local data and return json.
    """
    json_data = []
    label_info = {}
    json_tempfile = "jtmp{}{}.json".format(time.time(), random.random())

    try:
        file_reader = open(file_path, 'r', encoding='utf-8')
        file_reader.read()
        file_reader.seek(0)
    except UnicodeDecodeError:
        file_reader = open(file_path, 'r', encoding='gbk')
    flag = False
    for line in file_reader:
        # print(line, len(line.strip()))
        if len(line.strip()) == 0:
            continue
        # line = line.replace('""', '"').replace('\\', '\\\\')
        try:
            one = json.loads(line)
        except:
            flag = True
            with open(json_tempfile,'w',encoding='utf-8') as f:
                json.dump(line, f)
                print("save json")
            with open(json_tempfile,'r',encoding='utf-8') as file:
                one=json.load(file)
        json_data.append(one)
        if task_type == 'classification':
            if test and one['label'] not in label_info:  # 只能从测试集获取label info
                if info:
                    label_info[one['label']] = one['label_des']
        elif task_type == 'ner':  # TODO check what ner label info needs
            pass
        else:
            raise NotImplementedError
    full_data = {'json': json_data, 'info': label_info}
    if flag:
        os.remove(json_tempfile)
    return full_data


def read_data_oss(file_path: str, test: bool = False, info: bool=True, task_type: str='classification') -> Dict:
    """
        read data from oss and return json.
    """
    json_data = []
    label_info = {}
    local_tempfile = "tmp{}{}.json".format(time.time(), random.random())
    json_tempfile = "jtmp{}{}.json".format(time.time(), random.random())

    urllib.request.urlretrieve(file_path, local_tempfile)
    try:
        file_reader = open(local_tempfile, 'r', encoding='utf-8')
        file_reader.read()
        file_reader.seek(0)
    except UnicodeDecodeError:
        file_reader = open(local_tempfile, 'r', encoding='gbk')
    flag = False
    for line in file_reader:
        # print(line)
        if len(line.strip()) == 0:
            continue
        # print(line)
        # line = line.replace('\\', '\\\\')
        try:
            one = json.loads(line)
        except:
            flag = True
            with open(json_tempfile,'w',encoding='utf-8') as f:
                json.dump(line, f)
                print("save json")
            with open(json_tempfile,'r',encoding='utf-8') as file:
                one=json.load(file)
        json_data.append(one)
        if task_type == 'classification':
            if test and one['label'] not in label_info:  # 只能从测试集获取label info
                if info:
                    label_info[one['label']] = one['label_des']
        elif task_type == 'ner':  # TODO check what ner label info needs
            pass
        else:
            raise NotImplementedError
    full_data = {'json': json_data, 'info': label_info}
    os.remove(local_tempfile)
    if flag:
        os.remove(json_tempfile)
    return full_data


def read_data(file_path: str, test: bool = False, info: bool=True, task_type: str='classification') -> Dict:
    """
        warpper for read data
    """
    if file_path.startswith('http'):
        return read_data_oss(file_path, test, info, task_type)
    else:
        return read_data_local(file_path, test, info, task_type)


def extract_text_label(json_data: Dict, train: bool = True, task_type: str='classification') -> Tuple[List, List]:
    """
        抽取为list
    """
    text, label = [], []
    if train:  # train data is data withoutlabel
        if task_type == 'classification':
            [text.append(line['sentence'])
            for _, line in enumerate(json_data['json'])]
        elif task_type == 'ner':
            # for _, line in enumerate(json_data['json']):
            #     print('line=', line)
            #     print(line['text'])
            [text.append(line['text']) for _, line in enumerate(json_data['json'])]
    else:
        if task_type == 'classification':
            [(text.append(line['sentence']), label.append(int(line['label'])))
            for _, line in enumerate(json_data['json'])]
        elif task_type == 'ner':
            # doccano format
            entities, relations = [], []
            [(text.append(line['text']),
              entities.append(line['entities']),
              relations.append(line['relations'])) for _, line in enumerate(json_data['json'])]
            # entities = [line['entities'] for _, line in enumerate(json_data['json'])]
            # relations = [line['relations'] for _, line in enumerate(json_data['json'])]
            label = [entities, relations]
        else:
            raise NotImplementedError
    return text, label

    
def filter_unlabeled_list(X: List, y: List, L: List) -> Tuple:
    """
        filter list to ignore -1 entry.
    """
    mask = (np.array(L) != -1).any(axis=1)
    mask = np.where(mask)[0]
    return np.array([X[i] for i in mask]), np.array([y[i] for i in mask])


def add_train_labels(data: Dict, L: List) -> Dict:
    """
        add labels to orignal data.
    """
    for idx, line in enumerate(data['json']):
        data['json'][idx]['label'] = L[idx]
    return data


def generate_mapping(label_matrix: List[List]) -> BidictBase:
    """
        if label is not contious, make it contious.
    """
    np_matrix = np.array(label_matrix)
    uniques = np.unique(np_matrix).tolist()
    if -1 in uniques:
        uniques.remove(-1)
    if len(uniques) == max(uniques)+1:
        mapping = bidict({-1:-1})
    else:
        mapping = {-1:-1}
        for idx, item in enumerate(uniques):
            mapping[item] = idx
        mapping = bidict(mapping)
    return mapping


def generate_mapping_list(label_list: List) -> BidictBase:
    """
        if label is not contious, make it contious.
    """
    label_list = np.array(label_list)
    uniques = np.unique(label_list).tolist()
    if -1 in uniques:
        uniques.remove(-1)
    # if len(uniques) == max(uniques)+1:
    #     mapping = bidict({-1:-1})
    # else:
    mapping = {-1:-1}
    for idx, item in enumerate(uniques):
        mapping[item] = idx
    mapping = bidict(mapping)
    return mapping


def generate_mapping_list_from_str(label_list: List) -> BidictBase:
    """
        deal with string label.
    """
    label_list = np.array(label_list)
    uniques = np.unique(label_list).tolist()
    if -1 in uniques:
        uniques.remove(-1)
    if len(uniques) == max(uniques)+1:
        mapping = bidict({-1:-1})
    else:
        mapping = {-1:-1}
        for idx, item in enumerate(uniques):
            mapping[item] = idx
        mapping = bidict(mapping)
    return mapping


def preprocess_label_matrix(label_matrix: List[List], mapping: BidictBase) -> List[List]:
    """
        process the labeling matrix.
    """
    if len(mapping) == 1:
        return label_matrix
    else:
        new_label_matrix = []
        for row in label_matrix:
            new_row = []
            for item in row:
                new_row.append(mapping[item])
            new_label_matrix.append(new_row)
        return new_label_matrix


def preprocess_labels(labels: List, mapping: BidictBase) -> List:
    """
        process the labeling matrix.
    """
    if len(mapping) == 1:
        return labels
    else:
        new_labels = []
        for item in labels:
            new_labels.append(mapping[item])
        return new_labels


def postprocess_label(labels: List, mapping: Dict):
    """
        map the label back to original label.
    """
    if len(mapping) == 1:
        return labels
    new_labels = []
    for item in labels:
        new_labels.append(mapping.inverse[item])
    return new_labels
    

def getfromoss(remote_path: str, file_path: str):
    """
        download file from oss.
    """
    urllib.request.urlretrieve(remote_path, file_path)