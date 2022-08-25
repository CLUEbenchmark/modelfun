import sys

import transformers
sys.path.append('.')
sys.path.append('../')
import openai
import json
import os
import torch
import time
import random
from typing import List
from transformers import pipeline
from transformers import  BertTokenizer, BertModel
from paddlenlp import Taskflow
from tqdm import tqdm
import numpy as np
from scipy.spatial.distance import cosine
from tqdm import tqdm
from modelfun.utils.save import save_list_to_oss
from modelfun.utils.dataset import read_data, extract_text_label
from modelfun.utils.devices import get_gpu_id
KEY = os.getenv("OPENAI_API_KEY")
openai.api_key = 'test' if KEY is None else KEY


def gpt_single_classify(sentence: str, sample_list: List, label_list: List):
    response = openai.Classification.create(
        examples= sample_list,
        query=sentence,
        search_model="ada", 
        model="davinci",
        temperature=0, 
        labels=label_list,
        logprobs=len(label_list)+1,
    )
    return response


def xlm_roberta(sentences: List, examples: List, threshold: float=0, batch_size=64):
    classifier = pipeline("zero-shot-classification",
                          device=get_gpu_id(),
                          model="joeddav/xlm-roberta-large-xnli",
                        #   model='MoritzLaurer/mDeBERTa-v3-base-mnli-xnli',
                          )
    labels = np.unique([i[1] for i in examples]).tolist()
    try:
        response = classifier(sentences, labels, batch_size=batch_size)
    except RuntimeError as e:
        if 'out of memory' in str(e):
            print('| WARNING: ran out of memory, retrying batch')
            torch.cuda.empty_cache()
            response = classifier(sentences, labels, batch_size=64)
    # print(response)
    # prase response.
    pred_labels = []
    for res in response:
        idx = np.array(res['scores']).argmax()
        if res['scores'][idx] > threshold:
            pred_labels.append(res['labels'][idx])
        else:
            pred_labels.append("unknown")
    return pred_labels


def paddle_sim(sentences: List, examples: List, threshold: float=0, batch_size=512):
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
        for i in tqdm(range(0, len(all_pairs), 1000)):
            tmp_response = similarity(all_pairs[i:i+1000])
            response.extend(tmp_response)
    except RuntimeError as e:
        print(e)
        if 'out of memory' in str(e):
            print('| WARNING: ran out of memory, retrying batch')
            torch.cuda.empty_cache()
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


def clustering_bert(sentences: List, examples: List, threshold: float=0, batch_size=64):
    """
        Use clustering to speedup and stablize the content comparision.
        each example in sentences will compare to the average of each class.
    """
    # print('clustering ' * 100)
    # device = "cuda:0" if torch.cuda.is_available() else "cpu"
    # tokenizer = BertTokenizer.from_pretrained('peterchou/simbert-chinese-base').to(device)
    # model = BertModel.from_pretrained("peterchou/simbert-chinese-base").to(device)    
    # def get_features(text, batch_size):
    #     features = []
    #     for idx in tqdm(range(0, len(text), batch_size)):
    #         encoded_input = tokenizer(text[idx: idx+batch_size], return_tensors='pt', truncation=True, max_length=512, padding=True)
    #         model_outputs = model(**encoded_input)
    #         features.extend(model_outputs[0].tolist())
    #     return features
    feature_extractor = pipeline(task="feature-extraction", 
                                 device=get_gpu_id(),
                                 model="peterchou/simbert-chinese-base", truncation=True, max_length=512, padding=True)
    res = feature_extractor([e[0][:500] for e in examples], batch_size=batch_size, truncation=True , max_length=512)
    # res = get_features([e[0] for e in examples], batch_size)
    class_rep = {}
    for idx, exe in enumerate(examples):
        if exe[1] not in class_rep:
            class_rep[exe[1]] = []
        class_rep[exe[1]].append(res[idx][0][-1])
    # normalize the probs
    for key, value in class_rep.items():
        class_rep[key] = np.array(class_rep[key]).mean(axis=0)
    # add some other probs
    query_rep = feature_extractor([s[:500] for s in sentences], batch_size=64, truncation=True, max_length=512)
    # query_rep = get_features(sentences, batch_size)
    pred_labels = []
    for idx, sentence in enumerate(sentences):
        rep = query_rep[idx][0][-1]
        min_distance = 100
        label = -1
        for key, value in class_rep.items():  # compare with each class
            distance =  cosine(class_rep[key], rep)
            if distance < min_distance:
                min_distance = distance
                label = key
        # print(distance, label)
        if distance < threshold:
            pred_labels.append(label)
        else:
            pred_labels.append("unknown")
    return pred_labels


def gpt_model(sentences: str, sample_list: List, label_list: List):
    predicts = []
    for text in sentences:
        res = gpt_single_classify(text, sample_list, label_list)
        predicts.append(res['label'])
    return predicts


def pretrained_lf(texts: str, model_name: str, examples: List, labels: List, oss: bool=True) -> str:
    # read from oss
    data = read_data(texts)
    texts, _ = extract_text_label(data)
    if model_name == 'gpt3':
        predicts = gpt_model(texts, examples, labels)
    elif model_name == 'sim':
        predicts = paddle_sim(texts, examples, threshold=0.3)
    elif model_name == 'roberta':
        predicts = xlm_roberta(texts, examples, threshold=0.3)
    elif model_name == 'clustering':
        predicts = clustering_bert(texts, examples, threshold=0.5)
    if oss:
        # results save to oss
        oss_file_name  = '{}{}gpt_res'.format(time.time(), random.random())
        save_list_to_oss(predicts, oss_file_name)
        return oss_file_name
    else:
        return predicts


def pretrained_lf_test(ttexts: str, model_name: str, examples: List, labels: List):
    texts = [ttexts]
    if model_name == 'gpt3':
        predicts = gpt_model(texts, examples, labels)
    elif model_name == 'sim':
        predicts = paddle_sim(texts, examples, threshold=0.3)
    elif model_name == 'roberta':
        predicts = xlm_roberta(texts, examples, threshold=0.3)
    elif model_name == 'clustering':
        predicts = clustering_bert(texts, examples, threshold=0.5)
    return predicts[0]


if __name__ == '__main__':
    res = clustering_bert(sentences=['今天天气真不错', '你说啥呢'], 
            examples=[['天天有个好心情', '正面'], ['屋漏偏逢连夜雨', '负面']], threshold=0.5)
    print(res)
