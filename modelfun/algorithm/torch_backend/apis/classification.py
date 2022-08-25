import os
from fastapi import APIRouter, HTTPException, BackgroundTasks
import requests
from typing import List, Tuple, Dict, Optional
import traceback
from uvicorn.config import LOGGING_CONFIG
import sys
sys.path.append('.')
sys.path.append('../')
from modelfun.labeling.run_lm import train_lm, lm_predict
from apis.info import DatasetInput, CodeInput, CodeInputTest, LabelModelInput, ClassificationInput, KeywordInput, AutoClsInput, FewshotInput
from modelfun.models.berts import bert_cls, bert_cls_from_label
from modelfun.labeling.function_template import lf_label, lf_label_test
from modelfun.models.logistic import logistic_cls, logistic_cls_from_label
from modelfun.models.berts import bert_cls
from modelfun.labeling.keyword_gen import keywords
from modelfun.labeling.autolf import generate_autolf
from modelfun.labeling.lf.fewshot import fewshot_cls
from modelfun.models.cls import postprocess_erine
paddle_port = os.getenv("PADDLE_PORT")
paddle_port = int(paddle_port) if paddle_port is not None else 6685
paddle_addr = os.getenv("PADDLE_ADDR")
paddle_addr = paddle_addr if paddle_addr is not None else '127.0.0.1'

router = APIRouter(
    prefix = '',
    tags = ['classification']
)


@router.post('/modelfunAI/keyword', status_code=201)
async def keyword(ds: KeywordInput, topk: int=20) -> Dict:
    """
    # 展示每一个类别对应的关键词
    这里应该用的是  val_path , 即不是最终的测试集，是给用户看的那部分
    返回值为dict，每一个类别编号是key，对应的关键词列表是value
    """
    try:
        res = keywords(ds.val_path, ds.num_class, topk)
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )
    return res


@router.post('/modelfunAI/lf', status_code=201)  # return data report
async def label_function(code: CodeInput) -> List:
    """
    # 执行用户写的标签函数：

    这里直接规定类别以 0 开始编号，然后以 -1 表示不作预测. 要求对所有情况都要返回一个值（可以为-1）
    页面方面直接函数结构是固定的, 只需要写内容
    ```
    def lf_contains_link(x):
        return 1 if "http" in x.text.lower() else -1
    ```
    如果不用这种编号，我们需要在页面上展示出一个类别名字对照表？比如SPAM对1，那就是下面这样
    ```
    def lf_contains_link(x):
        # Return a label of SPAM if "http" in comment text, otherwise ABSTAIN
        return SPAM if "http" in x.text.lower() else ABSTAIN
    ```

    返回值为每一个数据预测的list
    """
    try:
        res = lf_label(code.name, code.content, code.data_path)
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )
    return res


@router.post('/modelfunAI/lftest', status_code=201)  # return data report
async def label_function(code: CodeInputTest) -> int:
    """
    # 测试用户写的标签函数：
        这里输入包含一个测试句子，返回在这个句子上的结果。其余和 `/lf` 一致

    返回值为标签
    """
    try:
        res = lf_label_test(code.name, code.content, code.data_sample)
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )
    return res


def background_label_model(ds: DatasetInput, soft: bool=False) -> None:
    res = {}
    try:
        res['results'] = train_lm(ds.train_label_matrix, ds.num_class, soft)
        res['state'] = True
        res['detail'] = ''
    except Exception as e:
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


@router.post('/modelfunAI/labelmodel', status_code=201)  # return data report
async def label_model(ds: DatasetInput, background_tasks: BackgroundTasks, soft: bool = False) -> Dict:
    """
    输入为 labeling function 生成的标签。 注意这里需要保证每个类别至少要有条规则，至少三条规则
        这里需要的是 train_label_matrix
    # 返回 label model 的模型路径，以及mapping的路径，以及标注的训练集
    返回值为 dict
    """
    try:
        background_tasks.add_task(background_label_model, ds, soft)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


def background_label_model_predict(ds: DatasetInput, soft: bool=False) -> None:
    res = {}
    try:
        res['results'] = lm_predict(ds.label_model_path, ds.mapping_model_path, ds.num_class,
                                    ds.train_label_matrix, ds.val_label_matrix, ds.test_label_matrix, 
                                    ds.test_path, soft, oss=True)
        res['state'] = True
        res['detail'] = ''
    except Exception as e:
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


@router.post('/modelfunAI/labelmodelpredict', status_code=201)  # return data report
async def label_model_predict(ds: LabelModelInput, soft: bool = False) -> Dict:
    """
    输入为 labeling function 生成的标签。
        这里的输入为 
            - train_label_matrix: 未标注数据集。这个会返回给你对应的标签
            - val_label_matrix: 可见测试集。这个会返回给你对应的标签
            - test_label_matrix: 不可见测试集。这个会返回给你对应的标签和评测指标
        随便你输入哪一个或者两个都输入，没有输入的部分会返回None

    直接返回开始预测(string)，然后会通过回调返回Dict，包含：聚合后的标签list以及性能指标（在result里面）
    """
    try:
        return lm_predict(ds.label_model_path, ds.mapping_model_path, ds.num_class,
                     ds.train_label_matrix, ds.val_label_matrix, ds.test_label_matrix, 
                     ds.test_path, soft, oss=True)
        # background_tasks.add_task(background_label_model_predict, ds, soft)
        # return {"message": "Start predict."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


def background_train(ds: DatasetInput, model: str) -> None:
    try:
        if model == 'lr':
            res = logistic_cls(ds.train_path, ds.test_path, ds.train_label_matrix, ds.num_class)
        elif model == 'bert':
            res = bert_cls(ds.train_path, ds.test_path, ds.train_label_matrix, ds.num_class)
        res['state'] = True
        res['detail'] = ''
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
    print(r, res)


@router.post('/modelfunAI/train', status_code=201)
async def train(ds: DatasetInput, model: str, background_tasks: BackgroundTasks) -> Dict:
    """
    # 直接根据LF训练一个模型
        这里要求每一类至少有一个lf
        根据当前生成的标签训练一个模型，训练并返回相应的metrics

      model 表示最终分类模型的类别，当前支持：
        - lr: 逻辑回归模型（速度快，当前机器配置下推荐使用）
        - bert: Bert模型，虽然我也尽量选了计算代价低的，但是相比LR还是慢多了
    ```
    {'text':'label', 'text2':'label2'}
    ```
    返回值为当前的 metrics
    """
    simple = True  # use bert if the server is upgrade
    try:
        background_tasks.add_task(background_train, ds, model)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


def background_train_from_label(ds: ClassificationInput, model: str) -> None:
    try:
        labels = ds.label_model_prediction
        print(labels)
        from modelfun.utils.save import get_list_from_oss
        print('read', get_list_from_oss(labels))
        if model == 'lr':  # 传统模型
            res = logistic_cls_from_label(train_path= ds.train_path, 
                                        test_path=ds.test_path, 
                                        train_label=ds.train_label, 
                                        labeled_path=ds.labeled_path,
                                        label_model_predictions=ds.label_model_prediction)
        elif model == 'bert':  # 快速模型
            res = bert_cls_from_label(train_path=ds.train_path, 
                                      test_path=ds.test_path, 
                                      train_label=ds.train_label, 
                                      num_class=ds.num_class, 
                                      labeled_path=ds.labeled_path,
                                      label_model_predictions=ds.label_model_prediction)
        elif model == 'macbert':  # 标准模型
            res = bert_cls_from_label(train_path=ds.train_path, 
                                      test_path=ds.test_path, 
                                      train_label=ds.train_label, 
                                      num_class=ds.num_class, 
                                      labeled_path=ds.labeled_path,
                                      label_model_predictions=ds.label_model_prediction, pretrain='hfl/chinese-macbert-base')
        elif model =='erine':
            if False:
                res = bert_cls_from_label(train_path=ds.train_path, 
                                        test_path=ds.test_path, 
                                        train_label=ds.train_label, 
                                        num_class=ds.num_class, 
                                        labeled_path=ds.labeled_path,
                                        label_model_predictions=ds.label_model_prediction, pretrain='hfl/chinese-macbert-large')
            else:
                # res = {}
                # res['results'] = None
                # send requests
                header = {
                    'Connection': 'keep-alive',
                    'Content-Type': 'application/json',
                    'charset': 'utf-8'
                }
                print('send request')
                payload = {'train_path': ds.train_path, 'labeled_path': ds.labeled_path,
                        'num_class': ds.num_class, 'val_path': ds.val_path,
                        'train_label': ds.train_label, 'test_label': ds.test_label,
                        'test_path': ds.test_path, 'num_class': ds.num_class}
                r = requests.post('http://{}:{}/erinecls'.format(paddle_addr, paddle_port), json=payload, headers=header)
                
                if 'detail' in r.json():  # error
                    res = {}
                    res['state'] = False
                    res['detail'] = r.json()['detail']
                    res['labels'] = ''
                else:
                    res = r.json()
                    # print(res)
                    res = postprocess_erine(true_label=res['true'],
                                            predict_label=res['predict'],
                                            label_model_predictions=ds.label_model_prediction)
                    res['state'] = True
                    res['detail'] = ''
                    # TODO process this
                    # res['results'] = r.json()
            res['state'] = True
            res['detail'] = ''
    except Exception as e:
        print(str(e))
        res = {}
        res['state'] = False
        res['detail'] = str(e)
    res['record_id'] = ds.record_id
    res['task_id'] = ds.task_id
    header = {
        'Connection': 'keep-alive',
        'Content-Type': 'application/json'
    }
    print(res)
    r = requests.post(ds.callback, json = res, headers=header)
    print(r)


@router.post('/modelfunAI/trainfromlabel', status_code=201)
async def train_from_label(ds: ClassificationInput, model: str, background_tasks: BackgroundTasks) -> Dict:
    """
    # 根据聚合的 label 来生成相应的标签【就是 /label 接口返回的部分】

      model 表示最终分类模型的类别，当前支持：
        - lr: 逻辑回归模型（速度快，当前机器配置下推荐使用）
        - bert: Bert模型，虽然我也尽量选了计算代价低的，但是相比LR还是慢多了
    ```
    {'text':'label', 'text2':'label2'}
    ```
    返回值为当前的 metrics
    """
    simple = True  # use bert if the server is upgrade
    try:
        background_tasks.add_task(background_train_from_label, ds, model)
        return {"message": "Start trainning."}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


@router.post('/modelfunAI/autocls', status_code=201)
async def auto_cls(ds: AutoClsInput) -> Dict:
    """
    # 自动生成 labeling function
    返回值为后端制定的 不同的 labeling function 的表示
    """
    try:
        res = generate_autolf(ds.train_path, ds.val_path, ds.test_path, ds.num_class, ds.labels)
        return res
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )


def background_fewshot(ds: FewshotInput, model) -> None:
    res = {}
    # model = 'fast'
    try:
        if model == 'fast':
            res['results'] = fewshot_cls(train_path=ds.train_path,
                                         unlabeled_path=ds.unlabeled_path,
                                         val_path=ds.val_path,
                                         test_path=ds.test_path,
                                         num_class=ds.num_class)
        elif model == 'ptunning':
            res['results'] = None
            # send requests
            header = {
                'Connection': 'keep-alive',
                'Content-Type': 'application/json',
                'charset': 'utf-8'
            }
            print('send request')
            payload = {'train_path': ds.train_path, 'unlabeled_path': ds.unlabeled_path,
                       'num_class': ds.num_class, 'val_path': ds.val_path,
                       'test_path': ds.test_path}
            r = requests.post('http://{}:{}/fewshotcls'.format(paddle_addr, paddle_port), json=payload, headers=header)
            # print('receieved', r)
            # print(r.json())

            if 'detail' in r.json():  # error
                res['state'] = False
                res['detail'] = r.json()['detail']
                res['labels'] = ''
            else:
                res['state'] = True
                res['detail'] = ''
                res['results'] = r.json()
        else:
            raise NotImplementedError
        res['state'] = True
        res['detail'] = ''
        print(res['state'])
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
    print(r, res)


@router.post('/modelfunAI/fewshot', status_code=201)
async def fewshot_cls_api(ds: FewshotInput, model: str, background_tasks: BackgroundTasks) -> Dict:
    """
    # 根据少量的标签生成一个 labeling function
        这里的输入是有标签的
        
    返回对于未标记数据的标注结果
    """
    try:
        background_tasks.add_task(background_fewshot, ds, model)
        return {"message": "Start trainning.", 'timeout': 3600}
    except Exception as e:
        print(traceback.format_exc())
        raise HTTPException(
            status_code=500,
            detail=str(e),
            headers={"X-Error": "Error"},
        )

