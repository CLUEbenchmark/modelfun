from typing import List

from sqlalchemy import func
from modelfun.labeling.lf.core import LabelingFunction
from modelfun.labeling.apply.core import  LFApplier
from modelfun.utils.dataset import read_data, extract_text_label


def lf_label(name: str, content: str, data_path: str) -> List:
    func_str = "def {}(x):\n{}".format(name, content)
    exec(func_str)
    lf = LabelingFunction(name, eval(name))
    applier = LFApplier([lf])

    # read data
    text_data = read_data(data_path)
    sentences, _ = extract_text_label(text_data)
    res = applier.apply(sentences)
    return [i[0] for i in res.tolist()]


def lf_label_test(name: str, content: str, data_sample: str) -> int:
    func_str = "def {}(x):\n{}".format(name, content)    
    exec(func_str)
    lf = LabelingFunction(name, eval(name))
    applier = LFApplier([lf])

    sentences = [data_sample]
    res = applier.apply(sentences)
    return res.tolist()[0][0]


if __name__ == '__main__':
    pass
