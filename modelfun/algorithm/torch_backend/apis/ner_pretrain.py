import requests
from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import List, Tuple, Dict, Optional
import traceback
from apis.info import NERInput, NERALLInput, NERLabelingInput
from modelfun.models.berts import bert_cls
import os
paddle_port = os.getenv("PADDLE_PORT")
paddle_port = int(paddle_port) if paddle_port is not None else 6685
paddle_addr = os.getenv("PADDLE_ADDR")
paddle_addr = paddle_addr if paddle_addr is not None else '127.0.0.1'
router = APIRouter(
    tags=['ner']
)


def background_ner_big_model_classify(ds: NERInput) -> None:
    res = {}
    try:
        if ds.model_name == 'uie':
            header = {
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'charset': 'utf-8'
            }
            # print('send request')
            payload = {'texts': ds.texts, 'schemas': ds.schemas,
                       'model_name': ds.model_name}
            r = requests.post('http://{}:{}/nerlf'.format(paddle_addr, paddle_port), json=payload, headers=header)
            # print('receieved', r)
            print(r.json())
            if 'detail' in r.json():
                res['state'] = False
                res['detail'] = r.json()['detail']
                res['labels'] = ''
            else:
                res['state'] = True
                res['detail'] = ''
                res['labels'] = r.json()
        else:
            raise NotImplementedError
    except Exception as e:
        print(e)
        res['labels'] = ''
        res['state'] = False
        res['detail'] = str(e)
    res['record_id'] = ds.record_id
    res['task_id'] = ds.task_id
    header = {
        'Connection': 'keep-alive',
        'Content-Type': 'application/json'
    }
    r = requests.post(ds.callback, json = res, headers=header)
    print(r, res)


@router.post('/modelfunAI/uielf', status_code=201)  # return data report
async def ner_big_label_function(code: NERInput, background_tasks: BackgroundTasks) -> Dict:
    """
    # 基于UIE的NER模型：  此API已废弃
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id），也可以直接字符串
            schemas: List  # 样例，格式为 ['目标1', '目标2', '目标3']
            labels: List  # 标签类别，这里需要用文本（example的标签也是）
        返回值为每一个数据预测的list，注意这里返回非数字，你需要自己做一个映射回去
    """
    try:
        background_tasks.add_task(background_ner_big_model_classify, code)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


def background_ner_allinone_classify(ds: NERALLInput) -> None:
    res = {}
    try:
        if ds.model_name == 'uie':
            header = {
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'charset': 'utf-8'
            }
            # print('send request')
            payload = {'unlabeled_path': ds.unlabeled_path, 
                       'test_path': ds.test_path,
                       'schemas': ds.schemas,
                       'model_name': ds.model_name}
            r = requests.post('http://{}:{}/nerallinone'.format(paddle_addr, paddle_port), json=payload, headers=header)
            # print('receieved', r)
            print(r.json())
            if 'detail' in r.json():
                res['state'] = False
                res['detail'] = r.json()['detail']
                res['results'] = {}
            else:
                res['state'] = True
                res['detail'] = ''
                res['results'] = r.json()
        else:
            raise NotImplementedError
        
    except Exception as e:
        print(e)
        res['results'] = ''
        res['state'] = False
        res['detail'] = str(e)
    res['record_id'] = ds.record_id
    res['task_id'] = ds.task_id
    header = {
        'Connection': 'keep-alive',
        'Content-Type': 'application/json'
    }
    r = requests.post(ds.callback, json = res, headers=header)
    print(r, res)


@router.post('/modelfunAI/nerallinone', status_code=201)  # return data report
async def ner_allinone_function(code: NERALLInput, background_tasks: BackgroundTasks) -> Dict:
    """
    # 一键运行NER：    此API已废弃
        传入参数有：
        unlabeled_path: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
        test_path: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
        schemas: List  # 样例，格式为 ['目标1', '目标2', '目标3']
    """
    try:
        background_tasks.add_task(background_ner_allinone_classify, code)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


def background_ner_tune_allinone_classify(ds: NERALLInput) -> None:
    res = {}
    try:
        if ds.model_name == 'uie':
            header = {
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'charset': 'utf-8'
            }
            # print('send request')
            payload = {'unlabeled_path': ds.unlabeled_path, 
                       'test_path': ds.test_path,
                       'tune_path': ds.tune_path,
                       'schemas': ds.schemas,
                       'model_name': ds.model_name}
            r = requests.post('http://{}:{}/uietune'.format(paddle_addr, paddle_port), json=payload, headers=header)
            # print('receieved', r)
            print(r.json())
            if 'detail' in r.json():
                res['state'] = False
                res['detail'] = r.json()['detail']
                res['results'] = {}
            else:
                res['state'] = True
                res['detail'] = ''
                res['results'] = r.json()
        else:
            raise NotImplementedError
        
    except Exception as e:
        print(e)
        res['results'] = ''
        res['state'] = False
        res['detail'] = str(e)
    res['record_id'] = ds.record_id
    res['task_id'] = ds.task_id
    header = {
        'Connection': 'keep-alive',
        'Content-Type': 'application/json'
    }
    r = requests.post(ds.callback, json = res, headers=header)
    # print(r, res)


@router.post('/modelfunAI/nertuneallinone', status_code=201)  # return data report
async def ner_nertune_function(code: NERALLInput, background_tasks: BackgroundTasks) -> Dict:
    """
    # 一键运行NER微调版本：
        传入参数有：
        unlabeled_path: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
        tune_path: str  # 用于微调数据，格式与test相同，需要有每一个目标的例子
        test_path: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
        schemas: List  # 样例，格式为 ['目标1', '目标2', '目标3']
    """
    try:
        background_tasks.add_task(background_ner_tune_allinone_classify, code)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


def background_ner_labeling(ds: NERLabelingInput) -> None:
    res = {}
    try:
        if ds.model_name == 'uie':
            header = {
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'charset': 'utf-8'
            }
            print('send request')
            payload = {'unlabeled_path': ds.unlabeled_path, 
                       'train_path': ds.train_path,
                       'test_path': ds.test_path,
                       'schemas': ds.schemas,
                       'model_name': ds.model_name}
            r = requests.post('http://{}:{}/uielabeling'.format(paddle_addr, paddle_port), json=payload, headers=header)
            print('receieved', r)
            print(r.json()['report'])
            if 'detail' in r.json():
                res['state'] = False
                res['detail'] = r.json()['detail']
                res['results'] = {}
            else:
                res['state'] = True
                res['detail'] = ''
                res['results'] = r.json()
        else:
            raise NotImplementedError
        
    except Exception as e:
        print(e)
        res['results'] = ''
        res['state'] = False
        res['detail'] = str(e)
    res['record_id'] = ds.record_id
    res['task_id'] = ds.task_id
    header = {
        'Connection': 'keep-alive',
        'Content-Type': 'application/json'
    }
    r = requests.post(ds.callback, json = res, headers=header)
    print(r, 'from ', ds.callback)


@router.post('/modelfunAI/nerlabeling', status_code=201)  # return data report
async def ner_labeing_function(code: NERLabelingInput, background_tasks: BackgroundTasks) -> Dict:
    """
    # 自动标注 
        传入参数有：
        train_path: str  # 文件下载文件路径，json格式，
        unlabeled_path: str  # 需要标记的数据
        schemas: List  # 样例，格式为 ['目标1', '目标2', '目标3']
    """
    try:
        background_tasks.add_task(background_ner_labeling, code)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )