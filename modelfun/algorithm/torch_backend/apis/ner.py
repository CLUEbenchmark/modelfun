from fastapi import APIRouter, HTTPException, BackgroundTasks
import requests
from typing import List, Tuple, Dict, Optional
import traceback
from uvicorn.config import LOGGING_CONFIG
from modelfun.labeling.run_lm import train_lm, lm_predict
from apis.info import NERTrainInput, NERSelectInput
from modelfun.models.bertner import train_ner
from modelfun.utils.dataset import read_data
import os
paddle_port = os.getenv("PADDLE_PORT")
paddle_port = int(paddle_port) if paddle_port is not None else 6685
paddle_addr = os.getenv("PADDLE_ADDR")
paddle_addr = paddle_addr if paddle_addr is not None else '127.0.0.1'
router = APIRouter(
    tags=['ner']
)


def background_ner_train(ds: NERTrainInput) -> None:
    res = {}
    try:
        tmp_data = read_data(ds.train_path)
        if len(tmp_data['json']) < 10000:
            ds.model_name = 'uie'
        if ds.model_name == 'macbert':
            res['results'] = train_ner(ds.train_path, ds.test_path, ds.unlabeled_path, ds.schemas, 'hfl/chinese-macbert-base'
            )
            res['state'] = True
            res['detail'] = ''
        if ds.model_name == 'roberta':
            res['results'] = train_ner(ds.train_path, ds.test_path, ds.unlabeled_path, ds.schemas, 'hfl/chinese-roberta-wwm-ext'
            )
            res['state'] = True
            res['detail'] = ''
        elif ds.model_name == 'uie':
            header = {
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'charset': 'utf-8'
            }
            payload = {'unlabeled_path': ds.unlabeled_path, 
                       'train_path': ds.train_path,
                       'test_path': ds.test_path,
                       'schemas': ds.schemas,
                       'model_name': ds.model_name}
            print('send request', payload)
            r = requests.post('http://{}:{}/uietrain'.format(paddle_addr, paddle_port), json=payload, headers=header)
            print('receieved', r)
            print(r.json())
            if 'detail' in r.json():
                res['state'] = False
                res['detail'] = r.json()['detail']
                res['results'] = {}
            else:
                res['state'] = True
                res['detail'] = ''
                res['results'] = r.json()
    except Exception as e:
        res = {}
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
    return res


@router.post('/modelfunAI/nertrain', status_code=201)
async def train(ds: NERTrainInput, background_tasks: BackgroundTasks, background: bool=True) -> Dict:
    """
    # ???????????? ???????????? ???????????? NER ??????
        ???????????????
          - unlabeled_path: str  # ?????????????????????
          - train_path: str # ??????????????????
          - test_path: str  # ??????????????????

      model ????????????????????????????????????????????????????????????????????????????????????
        - macbert

    ????????????????????? metrics?????????????????????????????????report?????????????????????dict
    """
    simple = True  # use bert if the server is upgrade
    print(ds)
    if background:
        try:
            background_tasks.add_task(background_ner_train, ds)
            return {"message": "Start trainning."}
        except Exception as e:
            print(traceback.format_exc())
            raise HTTPException(
                status_code=500,
                detail=str(e),
                headers={"X-Error": "Error"},
            )
    else:
        background_ner_train(ds)

def background_uie_select(ds: NERSelectInput) -> None:
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
            r = requests.post('http://{}:{}/uieselect'.format(paddle_addr, paddle_port), json=payload, headers=header)
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


@router.post('/modelfunAI/selectdata', status_code=201)  # return data report
async def ner_big_label_function(code: NERSelectInput, background_tasks: BackgroundTasks) -> Dict:
    """
    # ???????????????????????????   ???API?????????
        ??????????????????
            texts: str  # ???????????????????????????json??????????????????????????????????????????id???
            scheme: List  # ?????????????????????

        ???????????????????????? {'unlabel_res': res_all, 'certainty_idx': certainty_idx, 'uncertainty_idx': uncertainty_idx}??? ??????  uncertainty_idx ?????????????????????????????????
    """
    try:
        background_tasks.add_task(background_uie_select, code)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )
