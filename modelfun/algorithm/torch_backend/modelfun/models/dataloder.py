from typing import Dict, List
import torch


class ClassificationDataset(torch.utils.data.Dataset):
    def __init__(self, encodings: str, labels: str=None) -> None:
        self.encodings = encodings
        self.labels = labels

    def __getitem__(self, idx) -> Dict:
        item = {key: torch.tensor(val[idx])
                for key, val in self.encodings.items()}
        if self.labels is not None:
            item['labels'] = torch.tensor(self.labels[idx])
        return item

    def __len__(self):
        return len(self.encodings['input_ids'])



class CLUENERDataset(torch.utils.data.Dataset):
    def __init__(self, tokenizer_name="hfl/chinese-roberta-wwm-ext", data_json=[], schemes = ["组织机构", "姓名", "地址", "公司", "政府", "书名", "游戏", "电影", "职位", "景点"], split="train"):
        self.tokenizer_name = tokenizer_name
        # self.tokenizer = AutoTokenizer.from_pretrained(self.tokenizer_name)
        # self.max_len = 64
        # self.label_dict = json.loads(open("./cached_datasets/law_ner/label_dict.json").read())
        
        self.data = []
        
        # tag dict
        self.ner_tag_dict = {}
        self.ner_tag_dict["O"] = len(self.ner_tag_dict)
        for scheme in schemes:
            self.ner_tag_dict["B-" + scheme] = len(self.ner_tag_dict)
            self.ner_tag_dict["I-" + scheme] = len(self.ner_tag_dict)
        
        self.rev_ner_tag_dict = {v: k for k, v in self.ner_tag_dict.items()}
        # self.examples = {}
        # self.examples
        examples = {}
        examples["tokens"] = []
        examples["ner_tags"] = []
        for data in data_json:
            # line = line.strip()
            # line = line.replace("\n", '')
            # self.data.append(eval(line))
            # data = eval(line)
            sentence = data['text']
            token_labels = ['O'] * len(sentence)
            if split != "unlabeled":
                for e_dict in data['entities']:
                    start_offset = int(e_dict['start_offset'])
                    end_offset = int(e_dict['end_offset'])
                    label = e_dict['label']
                    token_labels[start_offset] = 'B-' + str(label)
                    for i in range(start_offset+1, end_offset):
                        token_labels[i] = 'I-' + str(label)
            token_labels = [self.ner_tag_dict[label] for label in token_labels]
            examples["ner_tags"].append(token_labels)
            token_list = list(sentence)
            examples["tokens"].append(token_list)
        self.examples = examples
        
    # def __getitem__(self, idx):
    #     return self.examples["input_ids"][idx], self.examples["token_type_ids"][idx], self.examples["attention_mask"][idx], self.examples["labels"][idx]
    
    # def __len__(self):
    #     return len(self.examples)
    
    # def collate_fn(self, batch):
    #     input_ids = [item[0] for item in batch]
    #     token_type_ids = [item[1] for item in batch]
    #     attention_mask = [item[2] for item in batch]
    #     labels = [item[3] for item in batch]
    #     return input_ids, token_type_ids, attention_mask, labels