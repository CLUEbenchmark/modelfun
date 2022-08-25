import os
import sys
import shutil
from paddlener.ner import uie
from utils.devices import get_gpu_id
uie_path="./uie"
# dataset_split="1.0_0.0_0"
# cached_models_path="./tmpcached_models/$dataset_split"
# cached_datasets_path="./tmpcached_datasets/$dataset_split"
# eval_dataset_path="./tmpcached_datasets/$dataset_split/dev"


def create_if_not_exists(folder):
    if not os.path.exists(folder):
        os.makedirs(folder)


def remove_if_exists(folder):
    if os.path.exists(folder):
         shutil.rmtree(folder)


def finetune(input_path: str, prefix: str):
    """
        Save file in tmp/prefix
    """
    remove_if_exists('./tmp/{}'.format(prefix))
    save_dir = './tmp/{}/cached_dataset/'.format(prefix)
    create_if_not_exists(save_dir)
    process_file_cmd = 'python ./uie/doccano.py --doccano_file {} --task_type "ext" --save_dir {} --split 0.9 0.1 0'.format(input_path, save_dir)
    os.system(process_file_cmd)

    train_path = './tmp/{}/cached_dataset/train.txt'.format(prefix)
    dev_path = "./tmp/{}/cached_dataset/dev.txt".format(prefix)
    save_dir = "./tmp/{}/models".format(prefix)
    create_if_not_exists(save_dir)
    finetune_cmd = 'python ./uie/finetune.py --train_path {} --dev_path {} \
    --save_dir {} \
    --learning_rate 1e-5 \
    --batch_size 16 \
    --max_seq_len 512 \
    --num_epochs 100 \
    --model "uie-base" \
    --seed 1000 \
    --logging_steps 10 \
    --valid_steps 100 \
    --device "gpu:{}"'.format(train_path, dev_path, save_dir, get_gpu_id())
    os.system(finetune_cmd)
    


if __name__ == '__main__':
    finetune('datasets/ner_ds/testdata.json', 'adfdas')