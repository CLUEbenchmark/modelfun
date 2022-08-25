FROM registry.baidubce.com/paddlepaddle/paddle:2.3.1-gpu-cuda11.2-cudnn8

COPY ./ /server/

WORKDIR /server/
RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple setuptools_scm
RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple -r requirements.txt
RUN mkdir tmp
RUN ls
ENV PYTHONPATH "${PYTHONPATH}:/server/"
ENV MINIO_OSS_ID "minio-root-user"
ENV MINIO_OSS_SECRET "minio-root-password"
ENV CUDA_VISIBLE_DEVICES 0
ENV MINIO_ADDR "minio"
ENV LC_ALL "zh_CN.UTF-8"
ENV LANG "zh_CN.UTF-8"
ENV IP "0.0.0.0"
# RUN python apiserver.py
ENTRYPOINT ["python", "apiserver.py", "-p 6685"]
EXPOSE 6685
