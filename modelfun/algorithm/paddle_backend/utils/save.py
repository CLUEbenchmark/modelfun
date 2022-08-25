from http import client
import oss2
import os
import urllib
import random
import time
from typing import Any
import shutil
from minio import Minio

# for oss
access_key_id = os.getenv("OSS_ID") 
access_key_secret = os.getenv("OSS_SECRET")
access_key_id = access_key_id if access_key_id is not None else 'empty'
access_key_secret = access_key_secret if access_key_secret is not None else 'empty'

# for minio
minio_addr = os.getenv("MINIO_ADDR")
minio_addr = minio_addr if minio_addr is not None else '127.0.0.1'
minio_access_key_id = os.getenv("MINIO_OSS_ID") 
minio_access_key_secret = os.getenv("MINIO_OSS_SECRET")
minio_access_key_id = minio_access_key_id if minio_access_key_id is not None else 'empty'
minio_access_key_secret = minio_access_key_secret if minio_access_key_secret is not None else 'empty'

if access_key_id != 'empty':
    auth = oss2.Auth(access_key_id, access_key_secret)
    bucket = oss2.Bucket(auth, 'oss-cn-hangzhou.aliyuncs.com', 'modelfun')
else:
    client = Minio('{}:9000'.format(minio_addr),
                  access_key=minio_access_key_id,
                  secret_key=minio_access_key_secret,
                  secure=False)


def save2oss(file_path: str, file_name: str) -> None:
    # 必须以二进制的方式打开文件。
    # 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
    if access_key_id != 'empty':
        with open(file_path, 'rb') as fileobj:
            # Seek方法用于指定从第1000个字节位置开始读写。上传时会从您指定的第1000个字节位置开始上传，直到文件结束。
            # fileobj.seek(1000, os.SEEK_SET)
            # Tell方法用于返回当前位置。
            # current = fileobj.tell()
            # 填写Object完整路径。Object完整路径中不能包含Bucket名称。
            bucket.put_object(file_name, fileobj)
    else:
        client.fput_object("modelfun", file_name, file_path)


def getfromoss(remote_path: str, file_path: str):
    """
        download file from oss.
    """
    urllib.request.urlretrieve(remote_path, file_path)


def save_list_to_oss(var: Any, oss_file_name: str):
    """
        save to oss in readable format
            var: the object to upload
            oss_file_name: uploaded file name
    """
    tmp_file_name  = '{}{}local_tempfile'.format(time.time(), random.random())
    with open(tmp_file_name, 'w') as f:
        f.write(str(var).replace("'", '"'))
    save2oss(tmp_file_name, oss_file_name)
    os.remove(tmp_file_name)


def get_list_from_oss(remote_path: str):
    """
        download file from oss.
    """
    tmp_file_name  = '{}{}local_list_tempfile'.format(time.time(), random.random())

    urllib.request.urlretrieve(remote_path, tmp_file_name)
    with open(tmp_file_name, 'r') as f:
        res = eval(f.readline())
    os.remove(tmp_file_name)
    return res


def archive(output_filename, dir_name):
    shutil.make_archive(output_filename, 'zip', dir_name)


def from_minio():
    """
    docker run -p 9000:9000 --name minio \
    -d --restart=always \
    -e "MINIO_ROOT_USER=ROOTNAME" \
    -e "MINIO_ROOT_PASSWORD=CHANGEME123" \
    -v /home/data:/data \
    -v /home/config:/root/.minio \
    minio/minio server /data
    """
    client = Minio('127.0.0.1:9000',
                  access_key='ROOTNAME',
                  secret_key='CHANGEME123',
                  secure=False)
    # found = client.bucket_exists("asiatrip")
    # if not found:
    #     client.make_bucket("asiatrip")
    #     print('new file')
    # else:
    #     print("Bucket 'asiatrip' already exists")
    # upload
    client.fput_object("buk2", "1.jpg", "/opt/img/1.jpg")


def save2minio(file_path: str, file_name: str) -> None:
    client = Minio('127.0.0.1:9000',
                  access_key='ROOTNAME',
                  secret_key='CHANGEME123',
                  secure=False)
    client.fput_object("modelfun", file_name, file_path)


if __name__ == '__main__':
    from_minio()
