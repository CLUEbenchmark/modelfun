from cProfile import label
import numpy as np
from typing import Dict, List
from modelfun.utils.dataset import read_data, extract_text_label
import pkuseg
import jieba
import jieba.analyse

def keywords(val_path: str, num_class: int, topk: int=10) -> Dict:
    """
    extract top k frequent words from 
    """
    # TODO remove stop words.
    dataset = read_data(val_path, test=True, info=True)
    texts, labels = extract_text_label(dataset, train=False)
    num_class = num_class
    text_per_class = {}
    for c_id in np.unique(labels):
        sentence="".join([t for idx, t in enumerate(texts) if labels[idx]==c_id])
        # seg = pkuseg.pkuseg()           # 以默认配置加载模型
        # tags = seg.cut(sentence, nthread=20)  # 进行分词
        tags = jieba.analyse.textrank(sentence, topK=topk, withWeight=False, 
        # allowPOS=('nz', 'nt', 'n', 'vn', 'v')
        )
        text_per_class[c_id.item()] = list(tags)
    return text_per_class


if __name__ == '__main__':
    print(keywords('./datasets/cic/val.json', 100))
