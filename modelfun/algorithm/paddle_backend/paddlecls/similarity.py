import os
import time
import random
from typing import List
import paddle
from paddlenlp import Taskflow
import numpy as np
from tqdm import tqdm
from utils.datasets import read_data, extract_text_label
from utils.save import save_list_to_oss
from utils.devices import get_gpu_id


def paddle_sim(sentences: List, examples: List, threshold: float=0, batch_size=64):
    """
                100   2GB
                1000  6GB。
    """
    paddle.set_device('gpu:{}'.format(get_gpu_id()))

    all_pairs = []
    label_map = {i[0]:i[1] for i in examples}
    labels = [i[1] for i in examples]
    for sentence in sentences:
        for example in examples:
            all_pairs.append([sentence, example[0]])
    print('{} sentence pair in total'.format(len(all_pairs)))
    try:
        similarity = Taskflow("text_similarity", batch_size=batch_size, max_seq_len=128)
        response = []
        for i in tqdm(range(0, len(all_pairs), 1024)):
            tmp_response = similarity(all_pairs[i:i+1024])
            response.extend(tmp_response)
    except RuntimeError as e:
        print(e)
        if 'out of memory' in str(e):
            print('| WARNING: ran out of memory, retrying batch')
            # torch.cuda.empty_cache()
            similarity = Taskflow("text_similarity", batch_size=1, max_seq_len=128)
            response = similarity(all_pairs)
    pred_labels = []
    tmp_labels = []
    for idx, res in enumerate(response):
        tmp_labels.append(res['similarity'])
        if idx > 0 and (idx+1) % len(labels) == 0:
            index = np.array(tmp_labels).argmax()
            if tmp_labels[index]>threshold:
                pred_labels.append(labels[index])
            else:
                pred_labels.append("unknown")
            tmp_labels = []
            
    return pred_labels


def pretrained_lf(texts: str, model_name: str, examples: List, labels: List, oss: bool=True) -> str:
    # read from oss
    data = read_data(texts)
    print(data)
    texts, _ = extract_text_label(data)
    print(texts)
    if model_name == 'sim':
        predicts = paddle_sim(texts, examples, threshold=0.3)
    if oss:
        # results save to oss
        oss_file_name  = '{}{}gpt_res'.format(time.time(), random.random())
        save_list_to_oss(predicts, oss_file_name)
        return oss_file_name
    else:
        return predicts


def pretrained_lf_test(ttexts: str, model_name: str, examples: List, labels: List):
    texts = [ttexts]
    if model_name == 'sim':
        predicts = paddle_sim(texts, examples, threshold=0.3)
    return predicts[0]

