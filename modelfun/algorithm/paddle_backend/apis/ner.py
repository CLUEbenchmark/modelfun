import os
from fastapi import APIRouter, HTTPException, BackgroundTasks
import requests
from typing import List, Tuple, Dict, Optional
import traceback
import pickle
import random
import time
from uvicorn.config import LOGGING_CONFIG
from apis.info import GPTClassificationInput, NERInput, NERALLInput, NERSelectInput, NERLabelingInput, FewshotClsInput, ClassificationInput
from paddlecls.similarity import pretrained_lf, pretrained_lf_test
from paddlecls.ernie_cls import erine_classification
from paddlener.ner import ner_model, ner_allinone, uie_finetune, uie_select
from paddlecls.ptunning_cls import ptunning_fewshot
from utils.devices import get_gpu_id_cmd

IP_ADDR = os.getenv("IP") 
IP_ADDR = IP_ADDR if IP_ADDR is not None else 'localhost'

PYTHON_PATH = 'python'

router = APIRouter(
    prefix = '',
    tags = ['ner']
)


@router.post('/nerlf', status_code=201)  # return data report
def ner_label_function(ds: NERInput):
    """
    # NER 大模型，直接进行零样本学习：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            scheme: List  # 需要抽取的列表
    """
    try:
        res = ner_model(ds.texts, ds.model_name, ds.schemas)
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


@router.post('/nerallinone', status_code=201)  # return data report
def ner_allinone_api(ds: NERALLInput):
    """
    # NER 大模型：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            scheme: List  # 需要抽取的列表
    """
    try:
        res = ner_allinone(ds.unlabeled_path, ds.test_path, ds.model_name, ds.schemas)
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


@router.post('/uietune', status_code=201)  # return data report
def ner_allinone_tune_api(ds: NERALLInput):
    """
    # NER 大模型：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            scheme: List  # 需要抽取的列表
    """
    try:
        # unlabeled_path: str, tune_path: str, test_path: str, prefix: str,  model_name: str, schemas: List
        # res = uie_finetune(ds.unlabeled_path, ds.tune_path, ds.test_path, ds.model_name, ds.schemas)

        pickle_file = './tmp/{}{}pickle'.format(time.time(), random.random())
        with open(pickle_file, "wb") as myprofile:  
            pickle.dump([ds.unlabeled_path, ds.tune_path, ds.test_path, ds.model_name, ds.schemas, 'error_flag'], myprofile)
        os.system('CUDA_VISIBLE_DEVICES={} {} cmds.py --api uietune --inputs {}'.format(get_gpu_id_cmd(), PYTHON_PATH, pickle_file))
        
        with open(pickle_file, "rb") as get_myprofile:
            res = pickle.load(get_myprofile)
        os.remove(pickle_file)
        if isinstance(res, List) and res[-1] == 'error_flag':
            raise RuntimeError('资源紧张，请稍后再试')
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


@router.post('/uielabeling', status_code=201)  # return data report
def ner_allinone_tune_api(ds: NERLabelingInput):
    """
    # NER 大模型：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            scheme: List  # 需要抽取的列表
    """
    try:
        # res = uie_finetune(unlabeled_path = ds.unlabeled_path, 
        #                    tune_path = ds.train_path, 
        #                    test_path=ds.test_path, 
        #                    model_name = ds.model_name, 
        #                    schemas = ds.schemas)
        pickle_file = './tmp/{}{}pickle'.format(time.time(), random.random())
        with open(pickle_file, "wb") as myprofile:  
            pickle.dump([ds.unlabeled_path, ds.train_path, ds.test_path, ds.model_name, ds.schemas, 'error_flag'], myprofile)
        os.system('CUDA_VISIBLE_DEVICES={} {} cmds.py --api uielabeling --inputs {}'.format(get_gpu_id_cmd(), PYTHON_PATH, pickle_file))
        
        with open(pickle_file, "rb") as get_myprofile:
            res = pickle.load(get_myprofile)
        os.remove(pickle_file)
        if isinstance(res, List) and res[-1] == 'error_flag':
            raise RuntimeError('资源紧张，请稍后再试')
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


@router.post('/uietrain', status_code=201)  # return data report
def ner_train_api(ds: NERLabelingInput):
    """
    # NER 大模型：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            scheme: List  # 需要抽取的列表
    """
    try:
        # res = uie_finetune(unlabeled_path = ds.unlabeled_path, 
        #                    tune_path = ds.train_path, 
        #                    test_path=ds.test_path, 
        #                    model_name = ds.model_name, 
        #                    schemas = ds.schemas,
        #                    return_unlabeled=False,
        #                    uie_model='uie-base')
        pickle_file = './tmp/{}{}pickle'.format(time.time(), random.random())
        with open(pickle_file, "wb") as myprofile:  
            pickle.dump([ds.unlabeled_path, ds.train_path, ds.test_path, ds.model_name, ds.schemas, 'error_flag'], myprofile)
        os.system('CUDA_VISIBLE_DEVICES={} {} cmds.py --api uietrain --inputs {}'.format(get_gpu_id_cmd(), PYTHON_PATH, pickle_file))
        
        with open(pickle_file, "rb") as get_myprofile:
            res = pickle.load(get_myprofile)
        os.remove(pickle_file)
        if isinstance(res, List) and res[-1] == 'error_flag':
            raise RuntimeError('资源紧张，请稍后再试')
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


@router.post('/uieselect', status_code=201)  # return data report
def ner_select_api(ds: NERSelectInput):
    """
    # NER 切分数据：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            scheme: List  # 需要抽取的列表
    """
    try:
        # unlabeled_path: str, tune_path: str, test_path: str, prefix: str,  model_name: str, schemas: List
        res = uie_select(ds.unlabeled_path, ds.model_name, ds.schemas)
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )

