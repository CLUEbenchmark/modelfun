from typing import List
from collections import defaultdict, Counter
from modelfun.labeling.keyword_gen import keywords
from modelfun.utils.dataset import read_data


def generate_autolf(train_path: str, val_path: str, test_path: str, num_class: int, labels: List) -> List:
    print(labels)
    res_lfs = []
    used_id = [int(i['label_id']) for i in labels]
    data = read_data(test_path, test=True, info=True)
    # print(data['info'])
    # key words
    key_dict = keywords(val_path, num_class, topk=20)
    key_counter = Counter([item for sublist in key_dict.values() for item in sublist])
    # print(key_dict)
    for key, item in key_dict.items():
        # print(key, item)
        if key not in used_id:
            continue
        new_item = []
        for itm in item:
            if key_counter[itm] == 1:
                new_item.append(itm)
        if len(new_item) == 0:
            continue
        meta = {
            "ruleType": "3",
            "contentValue": "0",
            "keyword": "",
            "countType": "0",
            "gapType": "0",
            "include": "0",
            "lenType": "0",
            "len": "",
            "regex": ".*({}).*".format('|'.join(new_item))
        }
        lf = {
                "ruleName": "keyword_{}".format(key), # 必填
                "ruleType": "1",   # 规则类型，1：模式匹配，2：专家知识，3：查找数据库，4：外部系统，5：代码编写，6：内置模型，  必填
                "metadata": meta, # 规则内容，这是一个json格式的字符串，不同的规则类型对应的规则内容不同   必填
                "label": key, # 标签的ID    只有当ruleType为1的情况下才必填
                "labelDes": data['info'][int(key)] # 标签的描述   只有当ruleType为1的情况下才必填
            }
        # print(lf)
        res_lfs.append(lf)
    data = read_data(val_path, test=True, info=True)
    # print(data)

    # get unique data for each class as examples
    examples = []
    unique_labels = []
    unique_label_count = defaultdict(int)
    for line in data['json']:
        if int(line['label']) not in used_id:
            continue
        if unique_label_count[int(line['label'])] < 5:
            examples.append(line)
            unique_label_count[int(line['label'])] += 1
            if int(line['label']) not in unique_labels:
                unique_labels.append(int(line['label']))
    
    # content model
    # split this model to multiple smaller one
    meta = {'example': examples, 'labels': unique_labels, 'modelName': 2}
    lf= {
        "ruleName": "Content Model", 
        "ruleType": "6", 
        "metadata": meta 
    }
    res_lfs.append(lf)
    # model_class_size = 50
    # for idx, small_set_labels in enumerate([unique_labels[x:x+model_class_size] for x in range(0, len(unique_labels), model_class_size)]):
    #     small_set_examples = [line for line in examples if line['label'] in small_set_labels]
    #     meta = {'example': small_set_examples, 'labels': small_set_labels, 'modelName': 2}
    #     lf= {
    #         "ruleName": "Content Model {}".format(idx), 
    #         "ruleType": "6", 
    #         "metadata": meta 
    #     }
    #     res_lfs.append(lf)

    # label model
    meta = {'example': examples, 'labels': unique_labels, 'modelName': 3}
    lf = {
        "ruleName": "Label Model",
        "ruleType": "6", 
        "metadata": meta 
    }
    # res_lfs.append(lf)
    # for idx, small_set_labels in enumerate([unique_labels[x:x+model_class_size] for x in range(0, len(unique_labels), model_class_size)]):
    #     small_set_examples = [line for line in examples if line['label'] in small_set_labels]
    #     meta = {'example': small_set_examples, 'labels': small_set_labels, 'modelName': 3}
    #     lf= {
    #         "ruleName": "Label Model {}".format(idx), 
    #         "ruleType": "6", 
    #         "metadata": meta 
    #     }
    #     res_lfs.append(lf)

    # Clustering model
    meta = {'example': examples, 'labels': unique_labels, 'modelName': 4}
    lf = {
        "ruleName": "Fast Content Model",
        "ruleType": "6", 
        "metadata": meta 
    }
    res_lfs.append(lf)
    return res_lfs