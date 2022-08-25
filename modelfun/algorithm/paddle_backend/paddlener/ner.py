from cProfile import label
import os
import time
import random
from typing import List, Union
from paddlenlp import Taskflow
import numpy as np
from tqdm import tqdm
from utils.datasets import read_data, extract_text_label, getfromoss
from utils.save import save_list_to_oss, save2oss, archive
from paddlener.ner_metric import get_all_metrics
import sys
import shutil
import os
from utils.pathutils import remove_if_exists, create_if_not_exists
from utils.devices import get_gpu_id
from uie.model import UIE
import json
uie_path="./uie"


def split4uie(origianl_inputs):
    new_sentence = []
    sentence_tracker = []
    sentence_len = []
    for idx, sentence in enumerate(origianl_inputs):
        splits = sentence.split('。')
        for iidx, split in enumerate(splits):
            new_sentence.append(split)
            sentence_tracker.append(idx)
            if iidx == len(splits)-1:
                sentence_len.append(len(split))
            else:
                sentence_len.append(len(split)+1) # 除了最后一个
    return new_sentence, sentence_tracker, sentence_len


def merge4uie(uie_out, origianl_inputs, new_sentence, sentence_tracker, sentence_len):
    new_results = [{}] * len(origianl_inputs)
    pre_idx = 0
    tmp_res = {}
    tmp_len = 0
    for idx in range(len(uie_out)):
        if sentence_tracker[idx] == pre_idx:
            for label, values in uie_out[idx].items():
                for value in values:
                    value['start'] += tmp_len
                    value['end'] += tmp_len
                    if label in tmp_res:
                        tmp_res[label].append(value)
                    else:
                        tmp_res[label] = [value]
            tmp_len += sentence_len[idx]
        else:
            new_results[pre_idx] = tmp_res
            pre_idx = sentence_tracker[idx]
            tmp_res = {}
            tmp_len = 0
            # TODO add one item
            for label, values in uie_out[idx].items():
                for value in values:
                    tmp_res[label] = [value]
            tmp_len += sentence_len[idx]
    new_results[pre_idx] = tmp_res  # add final one
    return new_results


def split4file(filename):
    json_data = read_data(filename, test=True, task_type='ner')
    entities, relations = [], []
    text, label = [], []
    [(text.append(line['text']),
    entities.append(line['entities']),
    relations.append(line['relations'])) for _, line in enumerate(json_data['json'])]
    # split and write file in json line format.
    new_json = []
    for idx, tt in enumerate(text):  # each sentence
        splits = tt.split('。')
        current_len_start = 0
        current_len_end = 0
        for s_idx, ss in enumerate(splits):  # each split
            tmp_data = {}
            tmp_data['text'] = ss
            tmp_data['relations'] = []
            tmp_data['entities'] = []
            current_len_end = min(len(tt), current_len_start + len(ss) + 1)
            for entity in entities[idx]:
                if entity['start_offset'] >= current_len_start and entity['end_offset'] < current_len_end:
                    entity['start_offset'] -= current_len_start
                    entity['end_offset'] -= current_len_start
                    # print(current_len_start, entity)
                    tmp_data['entities'].append(entity)
            current_len_start = current_len_end
            # print(current_len_start)
            new_json.append(tmp_data)
    with open(filename, "w", encoding="utf-8") as f:
        for example in new_json:
            f.write(json.dumps(example, ensure_ascii=False) + "\n")


def uie(texts: Union[List, str], schemas: List, model_path=None):
    """
        directly run uie model. (pretrained model or given model path)
    """
    # TODO  split texts if its too long
    if model_path is None:
        ie = Taskflow('information_extraction', schema=schemas, batch_size=32)
    else:
        ie = Taskflow('information_extraction', schema=schemas, batch_size=32,
        task_path=model_path)
        # ie._model = UIE.from_pretrained(model_path)
        # ie._model.eval()
    new_sentence, sentence_tracker, sentence_len = split4uie(texts)
    tmp_results = ie(texts)
    results = merge4uie(tmp_results, texts, new_sentence, sentence_tracker, sentence_len)

    res_all = []
    num_id = 0
    for r in results:
        # print(r)
        res = {"relations": [], "entities": []}
        if len(r) > 0:
            for key, rx in r.items():
                for rx_item in rx:
                    res['entities'].append({'id': num_id,  
                                        'start_offset': rx_item['start'], 
                                        'end_offset': rx_item['end'],
                                        'label': key})
                    num_id += 1
        else:
            res['entities'].append(r)
        res_all.append(res)
    return res_all


def uie_with_score(texts: Union[List, str], schemas: List, model_path=None, train_len=None):
    """
        directly run uie model return score for each data sample.
    """
    # TODO  split texts if its too long
    if model_path is None:
        ie = Taskflow('information_extraction', schema=schemas, batch_size=32, model='uie-tiny')
    else:
        ie = Taskflow('information_extraction', schema=schemas, batch_size=32,
        task_path=model_path)
        # ie._model = UIE.from_pretrained(model_path)
        # ie._model.eval()
    results = ie(texts)
    res_all = []
    num_id = 0
    probabilities = []
    entities_length = []
    for r in results:  # each sentence
        # print(r)
        single_prob = []
        single_len = []
        res = {"relations": [], "entities": []}
        if len(r) > 0:
            labeled_span = {}
            used_item = []
            # which to add
            for key, rx in r.items():  # each label
                for rx_item in rx:  # each entity
                    already = False
                    for i in range(rx_item['start'], rx_item['end']):
                        if i in labeled_span: # and rx_item['probability'] < labeled_span[i]:
                            already = True
                            break
                        # else:
                        #     # remove from previous
                        #     if i in labeled_span:
                        #         used_item.remove((key, rx_item))
                    if already:
                        continue
                    used_item.append((key, rx_item))
                    for i in range(rx_item['start'], rx_item['end']):
                        labeled_span[i] = rx_item['probability']
            # real add
            for key, rx in r.items():  # each label
                for rx_item in rx:  # each entity
                    if (key, rx_item) not in used_item:
                        continue

                    res['entities'].append({'id': num_id,  
                                        'start_offset': rx_item['start'], 
                                        'end_offset': rx_item['end'],
                                        'label': key})
                    single_prob.append(rx_item['probability'])
                    single_len.append(rx_item['end'] - rx_item['start'])
                    num_id += 1
        else:
            res['entities'].append(r)
        probabilities.append(single_prob)
        res_all.append(res)
        entities_length.append(single_len)
    text_length = [len(i) for i in texts]
    ranking, raw_values = calculate_score(probabilities, entities_length, text_length, method='mix')
    # certainty must have annotated entities
    annotate_num = sum(raw_values>0)
    if train_len is None:
        bad_idx = min(annotate_num//2, int(len(ranking)*0.2))
        good_idx = annotate_num - bad_idx
        certainty_idx = ranking[:good_idx]
        uncertainty_idx = ranking[good_idx:annotate_num]
        train_idx = ranking[:min(100, good_idx//10)]
    else:
        # more train data more train index
        bad_idx = min(annotate_num//2, int(len(ranking)*0.2))
        good_idx = annotate_num - bad_idx
        certainty_idx = ranking[:good_idx]
        uncertainty_idx = ranking[good_idx:annotate_num]
        train_idx = ranking[:min(train_len//2, good_idx//20)]
    return {'unlabel_res': res_all, 'certainty_idx': certainty_idx.tolist(), 'uncertainty_idx': uncertainty_idx.tolist(), 'train_idx': train_idx.tolist()}


def calculate_score(probabilities: List, entities_length: List, text_length: List, method: str='least_entities'):
    """
        Calculate ranking based on these metrics.
    """
    if method in ['low_prob', 'high_prob']:  # lower is better
        if method  == 'low_prob':
            probs = [np.mean(i) for i in probabilities]
        else:
            probs = [-np.mean(i) for i in probabilities]
        return np.argsort(probs), probs
    elif method == 'least_entities':  # lower is better
        fraction = [np.sum(entities_length[i])/text_length[i] for i in range(len(text_length))]
        return np.argsort(fraction), fraction
    elif method == 'mix':
        # high prob and high ratio is better
        probs = [np.mean(i) for i in probabilities]
        fraction = [np.sum(entities_length[i])/text_length[i] for i in range(len(text_length))]
        mix = np.array(probs) + np.array(fraction)
        return np.argsort(-mix), mix
    else:
        raise NotImplementedError


def ner_model(texts: Union[str, List], model_name: str, schemas: List):
    if isinstance(texts, str):
        data = read_data(texts, task_type='ner')
        texts, _ = extract_text_label(data, task_type='ner')
    if model_name == 'uie':
        return uie(texts, schemas)
    else:
        raise NotImplementedError
    

def ner_allinone(unlabeled_path: str, labeled_path: str, model_name: str, schemas: List):
    unlabeled_data = read_data(unlabeled_path, task_type='ner')
    unlabeled_texts, _ = extract_text_label(unlabeled_data, task_type='ner')
    test_data = read_data(labeled_path, test=True, task_type='ner')
    test_texts, [test_entities, test_relations] = extract_text_label(test_data, train=False, task_type='ner')
    if model_name == 'uie':
        unlabel_res = uie(unlabeled_texts, schemas)
        test_res = uie(test_texts, schemas)
        # todo score it.
    accuracy, precision, recall, f1 = get_all_metrics(test_entities, test_res, test_texts, schemas)
    print(accuracy, precision, recall, f1)
    return {'label': unlabel_res, 'accuracy': accuracy, 'precision': precision, 'recall': recall, 'fscore': f1}


def uie_finetune(unlabeled_path: str, tune_path: str, test_path: str, model_name: str, schemas: List, return_unlabeled: bool=True, oss: bool=True,
uie_model: str='uie-tiny'):
    """
        Save file in tmp/prefix
    """
    # TODO get length of tune file. use it to select number of examples in training.
    data = read_data(tune_path, task_type='ner')
    train_len = len(data['json'])

    prefix = "{}{}prefix".format(time.time(), random.random())
    remove_if_exists('./tmp/{}'.format(prefix))
    save_dir = './tmp/{}/cached_dataset/'.format(prefix)  
    create_if_not_exists(save_dir)  
    if tune_path.startswith('http'):
        tune_path_local = save_dir+'data.json'
        getfromoss(tune_path, tune_path_local)
    else:
        tune_path_local = tune_path
    
    if test_path.startswith('http'):
        test_path_local = save_dir+'testdata.json'
        getfromoss(test_path, test_path_local)
    else:
        test_path_local = test_path

    # save data
    unlabeled_data = read_data(unlabeled_path, task_type='ner')
    unlabeled_texts, _ = extract_text_label(unlabeled_data, task_type='ner')
    
    if test_path is not None:
        test_data = read_data(test_path, test=True, task_type='ner')
        test_texts, [test_entities, test_relations] = extract_text_label(test_data, train=False, task_type='ner')

    # preprocess
    if False:
        process_file_cmd = 'python ./uie/doccano.py --doccano_file {} --task_type "ext" --save_dir {} --split 0.8 0.2 0'.format(tune_path_local, save_dir)
        os.system(process_file_cmd)
    else:
        split4file(tune_path_local)
        print('seperate')
        process_file_cmd = 'python ./uie/doccano.py --doccano_file {} --task_type "ext" --save_dir {} --split  --single_name train'.format(tune_path_local, save_dir)
        os.system(process_file_cmd)
        print('create train.txt', '#' * 10)

        split4file(test_path_local)
        process_file_cmd = 'python ./uie/doccano.py --doccano_file {} --task_type "ext" --save_dir {} --split  --single_name dev'.format(test_path_local, save_dir)
        os.system(process_file_cmd)
    # finetune
    train_path = './tmp/{}/cached_dataset/train.txt'.format(prefix)
    dev_path = "./tmp/{}/cached_dataset/dev.txt".format(prefix)
    save_dir = "./tmp/{}/models".format(prefix)
    create_if_not_exists(save_dir)
    finetune_cmd = 'CUDA_VISIBLE_DEVICES={} python ./uie/finetune.py --train_path {} --dev_path {} \
    --save_dir {} \
    --learning_rate 1e-5 \
    --batch_size 16 \
    --max_seq_len 512 \
    --num_epochs 10 \
    --model {} \
    --seed 1000 \
    --logging_steps 10 \
    --valid_steps 10 \
    --device "gpu"'.format(os.getenv("CUDA_VISIBLE_DEVICES") , train_path, dev_path, save_dir, uie_model)
    print(finetune_cmd)
    start = time.time()
    os.system(finetune_cmd)
    duration = time.time() - start
    print('tunning duration: ', duration)
    if duration < 120:
        return {'detail': 'GPU memory not enough! Please check the resources'}
    """
    GPU memory consumption
    erine-base 
        batch_size=64  44GB
        batch_size=48  34 GB
        batch_size=16  14 GB 
        batch_size=4  6 GB 
        batch_size=1  4GB
    """


    load_path = None
    if model_name in ['uie', 'macbert']:
        try:
            if os.path.exists(save_dir+'/model_best'):
                load_path = save_dir+'/model_best'
            else:
                dname = max([int(x.split('_')[1]) for x in os.listdir(save_dir)])
                load_path = save_dir+'/model_{}'.format(dname)
        except:
            load_path = None
            print('no model found, exit...')
            return  {'detail': 'GPU memory not enough! Please check the resources'}
        unlabel_res = uie_with_score(unlabeled_texts, schemas, model_path=load_path, train_len=train_len)
        # print(test_path is not None)
        if test_path is not None:
            test_res = uie_with_score(test_texts, schemas, model_path=load_path, train_len=train_len)
    
    if oss and load_path is not None:
        # save model
        # archive(load_path, load_path)
        # save2oss(load_path + '.zip', load_path + '.zip')
        # os.remove(load_path + '.zip')
        pass

    if test_path is not None:
        # print(test_entities, test_res)
        accuracy, precision, recall, f1, report = get_all_metrics(test_entities, test_res['unlabel_res'], test_texts, schemas)
        print(precision, recall, f1, report)

    shutil.rmtree('./tmp/{}'.format(prefix), ignore_errors=True)
    # print('*'*30)
    if test_path is not None:
        if return_unlabeled:
            return {'unlabel_res': unlabel_res['unlabel_res'], 'certainty_idx': unlabel_res['certainty_idx'], 'uncertainty_idx': unlabel_res['uncertainty_idx'], 
            'train_idx':  unlabel_res['train_idx'], 'accuracy': accuracy,
            'precision': precision, 'recall': recall, 'fscore': f1, 'report': report}
        else:
            return {'test_res': test_res['unlabel_res'], 'model_path': str(load_path)+'.zip', 'accuracy': accuracy,
            'precision': precision, 'recall': recall, 'fscore': f1, 'report': report}
    else:
        return unlabel_res



def uie_select(texts: Union[str, List], model_name: str, schemas: List):
    """
        Select initial labeling samples based on uie.
        We define a new metric based on probability and entity intensity.
    """
    if isinstance(texts, str):
        data = read_data(texts, task_type='ner')
        texts, _ = extract_text_label(data, task_type='ner')
    if model_name == 'uie':
        return uie_with_score(texts, schemas)


if __name__ == '__main__':
    texts = ["2月8日上午北京冬奥会自由式滑雪女子大跳台决赛中中国选手谷爱凌以188.25分获得金牌！",
    "2月8日上午北京冬奥会自由式滑雪女子大跳台决赛中中国选手谷爱凌以188.25分获得金牌！",
    "2月8日上午北京冬奥会自由式滑雪女子大跳台决赛中中国选手谷爱凌以188.25分获得金牌！",
            ]
    schema = ['时间', '选手', '赛事名称']
    print(ner_model(texts, 'uie', schema))
