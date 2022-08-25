from doctest import Example
from lib2to3.pgen2.token import OP
from pydantic import BaseModel
from typing import List, Tuple, Dict, Optional, Union


class ProjectInfo(BaseModel):  # 项目基本信息
    name: str
    domain_type: str  # 领域，如 医学、金融
    task_type: str  # 仅支持 "文本分类"
    key_words: Optional[str] = ""
    description: Optional[str] = ""
    data_source: Optional[str] = ""


class DatasetInput(ProjectInfo):
    train_path: str  # str  # 训练数据集地址；无标签的
    val_path: Optional[str]  # str  # 验证数据集地址，用于调参的
    test_path: str  # str  # 测试数据集地址；最终测试的

    num_class: int
    train_label_matrix: Union[str, List]  # 标注矩阵的文件名
    val_label_matrix: Optional[Union[str, List]]
    test_label_matrix: Optional[Union[str, List]]

    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]

# 推理请求信息
class ModelApiInput(BaseModel):
    model_name: Optional[str]
    task_name: Optional[str]
    task_type: Optional[str]
    labels: Optional[List]
    examples: Optional[list]
    return_likelihoods: Optional[str]
    generate_config: Optional[Dict]
    input_data: List

class AutoClsInput(ProjectInfo):
    train_path: str  # str  # 训练数据集地址；无标签的
    val_path: Optional[str]  # str  # 验证数据集地址，用于调参的
    test_path: str  # str  # 测试数据集地址；最终测试的
    num_class: int
    labels: List  # 标签集


class KeywordInput(ProjectInfo):
    train_path: Optional[str]  # str  # 训练数据集地址；无标签的
    val_path: str  # str  # 验证数据集地址，用于调参的
    test_path: Optional[str]  # str  # 测试数据集地址；最终测试的

    num_class: int


class LabelModelInput(ProjectInfo):  # label model 的预测
    train_path: str  # str  # 训练数据集地址；无标签的
    val_path: Optional[str]  # str  # 验证数据集地址，用于调参的
    test_path: str  # str  # 测试数据集地址；最终测试的

    label_model_path: str  # label model的路径
    mapping_model_path: str  # 不连续标签映射
    callback: Optional[str]
    task_id: Optional[int]
    record_id: Optional[int]

    num_class: int
    train_label_matrix: Optional[Union[str, List]]
    val_label_matrix: Optional[Union[str, List]]
    test_label_matrix: Optional[Union[str, List]]


class ClassificationInput(ProjectInfo):
    train_path: str  # str  # 训练数据集地址；无标签的
    val_path: Optional[str]  # str  # 验证数据集地址，用于调参的
    test_path: str  # str  # 测试数据集地址；最终测试的
    labeled_path: Optional[str]  # 可以有一些标记的数据一起送进来

    num_class: int
    train_label: str  # 训练集标签改为地址
    test_label: List = None
    label_model_prediction: Optional[Union[List, str]]

    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]
    

class CodeInput(BaseModel):
    name: str
    language: str = 'python'
    content: str
    data_path: str  # str  # 训练数据集地址；无标签的


class CodeInputTest(BaseModel):
    name: str
    language: str = 'python'
    content: str
    data_sample: str  # 测试样本


class GPTClassificationInput(BaseModel):
    texts: str  # 文件路径
    examples: List  # 样例，格式为 [['文本1', '标签1'], ['文本2', '标签2']]
    labels: List  # 标签类别
    model_name: str  # 模型类别，当前支持 'gpt3', 'sim', 'roberta'
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]


class GPTClassificationTestInput(BaseModel):
    texts: str  # 文件
    examples: List  # 样例，格式为 [['文本1', '标签1'], ['文本2', '标签2']]
    labels: List  # 标签类别
    model_name: str  # 模型类别，当前支持 'gpt3', 'sim', 'roberta'
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]


class FewshotInput(BaseModel):
    train_path: str  # str  # 训练数据集地址；有标签
    unlabeled_path: str  # str  # 需要标注的数据集地址
    test_path: Optional[str]
    val_path: Optional[str]
    num_class: int
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]


# Add NER inputs.


class NERInput(BaseModel):
    texts: str  # 文件路径
    schemas: List  # 样例，格式为 ['人物', '时间']
    model_name: str  # 模型类别，当前支持 'uie'
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]


class NERALLInput(BaseModel):  # one click
    unlabeled_path: str  # 未标注文件路径
    test_path: str  # 测试文件路径
    schemas: List  # 样例，格式为 ['人物', '时间']
    model_name: str  # 模型类别，当前支持 'uie'
    tune_path: Optional[str]  # 用于微调的数据集
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]


class NERTrainInput(ProjectInfo):  # one click
    unlabeled_path: str  # 未标注文件路径
    train_path: str # 训练文件路径
    test_path: str  # 测试文件路径
    schemas: List  # 样例，格式为 ['人物', '时间']
    model_name: str  # 模型类别，当前支持 'uie'
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]


class NERSelectInput(BaseModel):  # one click
    unlabeled_path: str  # 未标注文件路径
    schemas: List  # 样例，格式为 ['人物', '时间']
    model_name: str  # 模型类别，当前支持 'uie'
    # tune_path: Optional[str]  # 用于微调的数据集
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]


class NERLabelingInput(BaseModel):  # fine tune and labeling
    unlabeled_path: str  # 未标注文件路径
    schemas: List  # 样例，格式为 ['人物', '时间']
    model_name: str  # 模型类别，当前支持 'uie'
    train_path: str  # 用于训练的数据集
    test_path: str  # 测试文件路径
    task_id: Optional[int]
    record_id: Optional[int]
    callback: Optional[str]