import requests
from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import List, Tuple, Dict, Optional
import traceback
from apis.info import GPTClassificationInput, GPTClassificationTestInput
from modelfun.models.berts import bert_cls
from modelfun.labeling.lf.gpt import pretrained_lf, pretrained_lf_test
import os
paddle_port = os.getenv("PADDLE_PORT")
paddle_port = int(paddle_port) if paddle_port is not None else 6685
paddle_addr = os.getenv("PADDLE_ADDR")
paddle_addr = paddle_addr if paddle_addr is not None else '127.0.0.1'
router = APIRouter(
    prefix = '',
    tags = ['classification']
)


def background_gpt_classify(ds: GPTClassificationInput) -> None:
    res = {}
    try:
        if ds.model_name == 'sim':
            header = {
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'charset': 'utf-8'
            }
            print('send request', 'http://{}:{}/gptlf'.format(paddle_addr, paddle_port))
            payload = {'texts': ds.texts, 'examples': ds.examples,
                       'labels': ds.labels, 'model_name': ds.model_name}
            r = requests.post('http://{}:{}/gptlf'.format(paddle_addr, paddle_port), json=payload, headers=header)
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
            res['labels'] = pretrained_lf(ds.texts, ds.model_name, ds.examples, ds.labels)
            res['state'] = True
            res['detail'] = ''
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


@router.post('/modelfunAI/gptlf', status_code=201)  # return data report
async def gpt_label_function(code: GPTClassificationInput, background_tasks: BackgroundTasks) -> Dict:
    """
    # 基于GPT分类部分标签：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            examples: List  # 样例，格式为 [['文本1', '标签1'], ['文本2', '标签2']]
            labels: List  # 标签类别，这里需要用文本（example的标签也是）
        返回值为每一个数据预测的list，注意这里返回非数字，你需要自己做一个映射回去
    """
    try:
        background_tasks.add_task(background_gpt_classify, code)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )



@router.post('/modelfunAI/gptlftest', status_code=201)  # return data report
async def gpt_label_test_function(code: GPTClassificationTestInput) -> Dict:
    """
    # 基于GPT分类部分标签：
        传入参数有：
            texts: str  # 文件下载文件路径，json格式，和训练集一致（可以没有id）
            examples: List  # 样例，格式为 [['文本1', '标签1'], ['文本2', '标签2']]
            labels: List  # 标签类别，这里需要用文本（example的标签也是）
        返回值为每一个数据预测的list，注意这里返回非数字，你需要自己做一个映射回去
    """
    try:
        res = pretrained_lf_test(code.texts, code.model_name, code.examples, code.labels)
        return {"labels": res}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )
