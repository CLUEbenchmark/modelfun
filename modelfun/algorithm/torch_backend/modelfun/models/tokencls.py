'''
Created by: Xiang Pan
Date: 2022-06-06 21:02:24
LastEditors: Xiang Pan
LastEditTime: 2022-06-06 21:09:19
Email: xiangpan@nyu.edu
FilePath: /modelfun-algo/modelfun/models/tokencls.py
Description: 
'''

import torch.nn as nn
from transformers import AutoModelForTokenClassification, Trainer, TrainingArguments, AutoModelForSequenceClassification, AutoTokenizer

class TextNERModel(nn.Module):
    def __init__(self, backbone_name = "hfl/chinese-macbert-base", num_labels=-1) -> None:
        super().__init__()
        assert num_labels > 0 
        self.backbone = AutoModelForTokenClassification.from_pretrained(backbone_name, num_labels=num_labels)
        
    def forward(self, model_inputs):
        output = self.backbone(**model_inputs)
        return output
    
    
if __name__ == "__main__":
    model = TextNERModel()
    print(model)
    