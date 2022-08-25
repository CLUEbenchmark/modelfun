from pydantic import BaseModel
from typing import List, Tuple, Dict, Optional, Union


class ProjectInfo(BaseModel):
    name: Optional[str]
    domain_type: Optional[str]  #
    task_type: Optional[str]  # 仅支持 "文本分类"
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

    num_class: int
    train_label_matrix: Optional[Union[str, List]]
    val_label_matrix: Optional[Union[str, List]]
    test_label_matrix: Optional[Union[str, List]]


class ClassificationInput(ProjectInfo):
    train_path: str  # str  # 训练数据集地址；无标签的
    val_path: Optional[str]  # str  # 验证数据集地址，用于调参的
    test_path: str  # str  # 测试数据集地址；最终测试的

    num_class: int
    train_label: Union[str, List]  # 训练集标签改为地址
    test_label: Union[str, List] = None
    labeled_path: Optional[str]  # 可以有一些标记的数据一起送进来

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
    oss: str='true'  # 'true' or false
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


class FewshotClsInput(BaseModel):
    train_path: str  # str  # 训练数据集地址；有标签的输入
    val_path: Optional[str]  # str  # 验证数据集地址，用于调参的。这个部分可以没有
    test_path: Optional[str]  # str  # 测试数据集地址；最终测试的
    unlabeled_path: str  # 无标记数据

    num_class: int


# NER task
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