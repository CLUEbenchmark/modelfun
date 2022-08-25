import os
from fastapi import APIRouter, HTTPException, BackgroundTasks
import requests
from typing import List, Tuple, Dict, Optional
import traceback
import pickle
import time
import random
from uvicorn.config import LOGGING_CONFIG
from apis.info import GPTClassificationInput, NERInput, NERALLInput, NERSelectInput, NERLabelingInput, FewshotClsInput, ClassificationInput
# from paddlecls.similarity import pretrained_lf, pretrained_lf_test
# from paddlecls.ernie_cls import erine_classification
# from paddlener.ner import ner_model, ner_allinone, uie_finetune, uie_select
# from paddlecls.ptunning_cls import ptunning_fewshot
from utils.devices import get_gpu_id_cmd

IP_ADDR = os.getenv("IP") 
IP_ADDR = IP_ADDR if IP_ADDR is not None else 'localhost'

PYTHON_PATH = 'python'

router = APIRouter(
    prefix = '',
    tags = ['classification']
)


@router.post('/gptlf', status_code=201)  # return data report
def gpt_label_function(ds: GPTClassificationInput):
    """
    # 基于 paddle 的 similarity 的模型：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            examples: List  # 样例，格式为 [['文本1', '标签1'], ['文本2', '标签2']]
            labels: List  # 标签类别，这里需要用文本（example的标签也是）
        返回值为每一个数据预测的list，注意这里返回非数字，你需要自己做一个映射回去
    """
    try:
        if ds.oss == 'true':
            oss = True
        else:
            oss = False
        # res = pretrained_lf(ds.texts, ds.model_name, ds.examples, ds.labels, oss=oss)
        pickle_file = './tmp/{}{}pickle'.format(time.time(), random.random())
        with open(pickle_file, "wb") as myprofile:  
            pickle.dump([ds.texts, ds.model_name, ds.examples, ds.labels, oss, 'error_flag'], myprofile)
        os.system('CUDA_VISIBLE_DEVICES={} {} cmds.py --api sim --inputs {}'.format(get_gpu_id_cmd(), PYTHON_PATH, pickle_file))
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


@router.post('/erinecls', status_code=201)  # return data report
def erine_cls(ds: ClassificationInput):
    """
    # 基于 paddle 的 similarity 的模型：
        传入参数有：
            train_path: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            unlabeled_path: List  # 未标注的数据
        返回值为每一个数据预测的list，注意这里返回非数字，你需要自己做一个映射回去
    """
    try:
        # res = erine_classification(train_data_path=ds.train_path, 
        #                            test_data_path= ds.test_path, 
        #                            train_label_list=ds.train_label,
        #                            labeled_path=ds.labeled_path,
        #                            num_class=ds.num_class
        #                            )
        pickle_file = './tmp/{}{}pickle'.format(time.time(), random.random())
        with open(pickle_file, "wb") as myprofile:  
            pickle.dump([ds.train_path, ds.test_path, ds.train_label,ds.labeled_path,ds.num_class, 'error_flag'], myprofile)
        os.system('CUDA_VISIBLE_DEVICES={} {} cmds.py --api erninecls --inputs {}'.format(get_gpu_id_cmd(), PYTHON_PATH, pickle_file))
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


@router.post('/fewshotcls', status_code=201)  # return data report
def fewshot_cls(ds: FewshotClsInput):
    """
    # 基于 paddle 的 similarity 的模型：
        传入参数有：
            train_path: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            unlabeled_path: List  # 未标注的数据
        返回值为每一个数据预测的list，注意这里返回非数字，你需要自己做一个映射回去
    """
    try:
        # res = ptunning_fewshot(train_path=ds.train_path, 
        #                        unlabeled_path=ds.unlabeled_path, 
        #                        val_path=ds.val_path,
        #                        test_path=ds.test_path,
        #                        num_class=ds.num_class)
        pickle_file = './tmp/{}{}pickle'.format(time.time(), random.random())
        with open(pickle_file, "wb") as myprofile:  
            pickle.dump([ds.train_path, ds.unlabeled_path, ds.val_path, ds.test_path, ds.num_class, 'error_flag'], myprofile)
        os.system('CUDA_VISIBLE_DEVICES={} {} cmds.py --api ptune --inputs {}'.format(get_gpu_id_cmd(), PYTHON_PATH, pickle_file))
        with open(pickle_file, "rb") as get_myprofile:
            res = pickle.load(get_myprofile)
        os.remove(pickle_file)
        if (isinstance(res, List) and res[-1] == 'error_flag') or res is None:
            raise RuntimeError('资源紧张，请稍后再试')
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )
        
