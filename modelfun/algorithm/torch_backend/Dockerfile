FROM pytorch/pytorch:1.12.0-cuda11.3-cudnn8-runtime

COPY ./ /server/

# RUN pip install requests oss2 fastapi "uvicorn[standard]" paddlenlp bidict minio pynvml
# RUN pip install --upgrade paddlenlp>=2.0.0rc -i https://pypi.org/simple

WORKDIR /server/
RUN mkdir -p tmpmodel
# RUN conda env create -f environment.yml
# RUN conda activate mfun37
# RUN conda init bash
# RUN apt update
# RUN apt -y install build-essential zip
# RUN pip install cython
RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple setuptools_scm
RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple -r requirements.txt
ENV PYTHONPATH "${PYTHONPATH}:/server/"
ENV MINIO_OSS_ID "minio-root-user"
ENV MINIO_OSS_SECRET "minio-root-password"
ENV PADDLE_PORT 6685
ENV CUDA_VISIBLE_DEVICES 0
ENV PADDLE_ADDR "paddlealg"
ENV MINIO_ADDR "minio"
ENV IP "0.0.0.0"
ENV LC_ALL "zh_CN.UTF-8"
ENV LANG "zh_CN.UTF-8"
# RUN python apiserver.py
RUN wandb disabled
# RUN bash produce_release.sh

# WORKDIR /server/build/
# RUN python apiserver.py
ENTRYPOINT ["python", "apiserver.py", "-p 6674"]
EXPOSE 6674
