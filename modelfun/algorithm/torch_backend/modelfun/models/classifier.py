from typing import List, Any, Tuple, Dict
from datasets import load_metric, Metric
import numpy as np
from sklearn.metrics import f1_score, recall_score, precision_score, accuracy_score, classification_report
from sklearn.metrics import precision_recall_curve
import torch
from transformers import Trainer, TrainingArguments, AutoModelForSequenceClassification, AutoTokenizer
from transformers.file_utils import torch_required
import os
from modelfun.models.dataloder import ClassificationDataset
from modelfun.utils.dataset import read_data
import random
import urllib
from modelfun.utils.save import save2oss
from modelfun.utils.devices import get_gpu_id
# os.environ["CUDA_VISIBLE_DEVICE"] = '0'
os.environ["TOKENIZERS_PARALLELISM"] = 'false'
device_id = 0

def compute_metrics(eval_pred: List):
    predictions, labels = eval_pred
    predictions = np.argmax(predictions, axis=1)
    metric = load_metric("f1")
    return metric.compute(predictions=predictions, references=labels, average='macro')


class customTrainingArguments(TrainingArguments):
    def __init__(self, *args, **kwargs):
        super(customTrainingArguments, self).__init__(*args, **kwargs)
        
    @property
    @torch_required
    def device(self) -> "torch.device":
        """
        The device used by this process.
        Name the device the number you use.
        """
        # return torch.device("cuda:3")
        global device_id
        return torch.device("cuda:{}".format(device_id))

    @property
    @torch_required
    def n_gpu(self):
        """
        The number of GPUs used by this process.
        Note:
            This will only be greater than one when you have multiple GPUs available but are not using distributed
            training. For distributed training, it will always be 1.
        """
        # Make sure `self._n_gpu` is properly setup.
        # _ = self._setup_devices
        # I set to one manullay
        self._n_gpu = 1
        return self._n_gpu


class Classifier:
    def __init__(self, num_class: int, pretrain_name: str='hfl/rbtl3', metric: str='f1', 
                 model_path: str='../tmp', num_train_epochs: int=50,
                 warmup_steps: int=500,
                 **kwages: Any) -> None:
        self.num_class = num_class
        self.pretrain_name = pretrain_name  # 加载的预训练模型的名称
        self.model_path=model_path
        self.num_train_epochs = num_train_epochs
        self.warmup_steps = warmup_steps
        self.tokenizer = AutoTokenizer.from_pretrained(
            self.pretrain_name, do_lower_case=True)
        self.model = AutoModelForSequenceClassification.from_pretrained(
            self.pretrain_name, num_labels=self.num_class)


    def preprocessing(self, X: List, y: List=None):
        """ Preprocess the data
        """
        train_encodings = self.tokenizer(
            X, truncation=True, padding=True, max_length=32)
        return ClassificationDataset(train_encodings, y)

    def fit(self, X: List, y: List) -> None:
        self.train_dataset = self.preprocessing(X, y)
        global device_id
        device_id = get_gpu_id()
        training_args = customTrainingArguments(
            output_dir=self.model_path,
            num_train_epochs=self.num_train_epochs,  # total number of training epochs
            per_device_train_batch_size=256//1,  # batch size per device during training
            per_device_eval_batch_size=32,  # batch size for evaluation
            # number of warmup steps for learning rate scheduler
            warmup_steps=self.warmup_steps,
            learning_rate=3e-4 if 'electra' in self.pretrain_name else 2e-5,
            weight_decay=0.01,  # strength of weight decay
            save_total_limit=1,
            # logging_dir='../../tmplogs',  # directory for storing logs
            # logging_steps=10,
            # evaluation_strategy="epoch",
        )

        self.trainer = Trainer(
            # the instantiated 🤗 Transformers model to be trained
            model=self.model,
            args=training_args,  # training arguments, defined above
            train_dataset=self.train_dataset,  # training dataset
            # eval_dataset=self.val_dataset,  # evaluation dataset
            compute_metrics=compute_metrics,
        )
        self.trainer.train()

    def predict(self, X: List) -> float:
        """
        train a model to get estimation of each data point
        """
        test_dataset = self.preprocessing(X)
        test_outputs = self.trainer.predict(test_dataset)
        return test_outputs.predictions

    def score(self, pred: List, y: List):
        """
            We need transformation, so the input is pred and true value.
        """
        # test_dataset = self.preprocessing(X)
        # test_outputs = self.trainer.predict(test_dataset)
        # pred = test_outputs.predictions.argmax(axis=-1)
        
        # add filter to fileter unlabeled classes. to be delete in the future
        # unique_labels = np.unique(pred)
        # idxs = [idx for idx, i in enumerate(y) if i in unique_labels]
        # y, pred = [y[i] for i in idxs], [pred[i] for i in idxs]
        # end adding
        acc = accuracy_score(y, pred)
        precision = precision_score(y, pred, average='weighted')
        recall = recall_score(y, pred, average='weighted')
        f1 = f1_score(y, pred, average='weighted')
        report = classification_report(y, pred, output_dict=True)
        return acc, precision, recall, f1, report

    def save(self, model_path: str) -> None:
        """Save the model to the specified file path
        """
        torch.save([self.tokenizer, self.model], model_path)
        save2oss(model_path, model_path.split('/')[-1])
        os.remove(model_path)
        return model_path.split('/')[-1]

    def load(self, model_path: str) -> None:
        """Load a saved model
        """
        urllib.request.urlretrieve(
            model_path, '{}local_tempfile'.format(random.random()))
        self.tokenizer, self.model = torch.load(
            '{}local_tempfile'.format(random.random()))
        os.remove('{}local_tempfile'.format(random.random()))


if __name__ == '__main__':
    pass
