# Paddle algorithms
这个项目主要是为了解决 Paddlepaddle和torch环境冲突的问题
此项目对于后端不可见，只与modelfun-alg 进行交互
主要实现了对文心大模型的调用和基于它的分类和NER模型

# 主要目录
- paddlecls/    分类相关的代码
    - ernie_cls.py  文心作为backbone进行分类
    - ptunning_cls.py  ptunning算法
    - similarity.py    基于相似度的大模型
- paddlener/     NER相关的代码
    - ner.py   基于UIE的模型，实现了一些定制化的优化

- uie/  经过修改的uie相关代码，对长文本进行了优化
- utils/ 辅助代码
    - 存储数据
    - 读取uie
- apiserver.py   服务端
- info.py   字段定义
- cmds.py  为了支持GPU调度对原有api的封装


# 部署方法
部署环境变量均包含在Dockerfile中，主要包括
- MINIO_OSS_ID minio存储服务使用的
- MINIO_OSS_SECRET minio存储服务使用的密码
- CUDA_VISIBLE_DEVICES 使用的显卡列表

# 镜像编译
直接运行根据Dockerfile编译即可

