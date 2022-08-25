<div align="center">
  <img src="docs/logo.png" width="100"/>
  <div>&nbsp;</div>
  <div align="center">
    <b><font size="5">官方网站</font></b>
    <sup>
      <a href="https://www.modelfun.cn/home">
        <i><font size="4">访问</font></i>
      </a>
    </sup>
  <div>&nbsp;</div>
  
[📘使用手册](https://www.modelfun.cn/doc) |
[🛠️本地部署](docs/modelfun镜像使用说明.md) |
[🤔问题汇报](https://github.com/CLUEbenchmark/modelfun/issues) | 
[📰开源协议](https://github.com/CLUEbenchmark/modelfun/blob/master/LICENSE)
  
</div>&nbsp;</div>


## ModelFun 介绍

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**目录**


- [1. ModelFun简介](#1-ModelFun简介)
  - [1.1 以数据为中心的人工智能](#11-以数据为中心的人工智能)
  - [1.2 大模型技术](#12-大模型技术)
  - [1.3 ModelFun 主要功能](#13-modelfun-主要功能)
  - [1.4 ModelFun 演示](#14-modelfun-演示)
- [2. 安装](#2-安装)
  - [2.1. 环境依赖](#21-环境依赖)
  - [2.2. 使用docker进行部署](#22-使用docker进行部署)
- [引用](#引用)
<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# 1. ModelFun简介
## 1.1 以数据为中心的人工智能
2010-2020年间，通用backbone得到了长足发展，model-centric推动了机器学习的飞速发展和落地。在2021年data-centric时代来临后，业内针对Data-Centric vs Model-Centric谁才是机器学习最佳实践指南的讨论便从未停止。

从model-centric的角度来看，工程师更加偏爱从模型出发，努力通过特征工程使数据适合其模型。当现有模型无法充分解决问题时，他们将开发足以解决问题的新模型。

从Data-centric的角度来看，解决方案是调整数据。数据决定了模型的上限，如果数据质量较差，解决方案是找到一种方法来获取更高质量的数据，而不是找到更健壮的模型，或更好的特征工程。

人工智能和机器学习领域国际最权威学者之一——吴恩达，曾在2021年3月在MLOps的直播讲座时强调：数据质量比模型调优更为重要和有效，为此，吴恩达还举办了一系列Data-centric的比赛。CLUE作为国内最早倡导以数据为中心的组织，也积极倡导并搭建了首个数据为中心的项目DataCLUE。

## 1.2 大模型技术
语言大模型，又被称作基础模型（Foundation Model）是当前自然语言处理领域最重要的进展之一。随着越来越多的大模型被不断提出，使得小样本学习甚至零样本学习成为可能。
当前的大模型技术已经在文本生成等方面取得了巨大的成功。

当前，大模型已经成为了公司和业界间AI能力竞争的主战场，各个大厂和重要学术机构纷纷研发了许多大模型，模型参数量已经超过千亿。

然而，如何将已经蓬勃发展的大模型技术有效地应用在自动化数据标注中还处于非常初级的阶段。当前最为广泛的使用方式还是基于大模型的预标注和人工纠错。

## 1.3 ModelFun 主要功能
ModelFun 希望能够有效融合当前在大模型方面的进展，打造一个一站式的数据自动标注平台。同时，为了方便用户使用，我们提供了一套完整的前后端系统，使得没有技术能力的用户也可以顺畅地使用ModelFun完成数据标注。

当前 ModelFun 主要支持如下功能：
1. 任务管理。支持以任务的形式组织标注任务，并为其提供基本的增删改查功能。
1. 数据集管理。包括多种格式的数据集上传（如Excel，Json）、数据集版本控制等。
2. 多种标注规则支持。提供了包括内置模型、模式匹配、专家系统、数据库系统、外部查询等多种标注规则。
2. 大模型自动标注。基于当前大模型技术，提供了多种大模型自动标注功能。当前开源版本包含了百度等企业开源的大模型。
3. 小样本学习。基于少量训练样本支持基于小样本的训练方法，适合于快速训练可用模型。
6. 弱标签集成。采用当前最先进的弱监督学习方法，有效聚合带噪声的标签，进一步提升标注精度。
7. 一键标注。支持数据集上传后一键完成数据标注任务。最大化提升标注效率。
8. 模型API部署。支持训练好的模型以API形式对外提供服务。


## 1.4 ModelFun 演示
你可以访问[这个网页](https://www.modelfun.cn/introduce)查看对于ModelFun的一个简单介绍。

如果你对于 ModelFun 有兴趣并且期待一些更为深入的合作你可以[申请演示](https://www.modelfun.cn/contact)。


# 2. 安装
ModelFun支持本地安装模式，为了进一步方便用户使用，我们提供了基于Docker的安装模式。
## 2.1. 环境依赖

1. 英伟达显卡驱动 https://www.nvidia.cn/geforce/drivers/.

2. Docker 安装:

>  * 安装 Docker 和 Docker Compose https://docs.docker.com/get-docker/
>  * 安装 NVIDIA Docker https://github.com/NVIDIA/nvidia-docker

3. 硬件推荐

> * 由于需要使用一些较大的模型，我们推荐使用的NVIDIA GPU 最好有大于24GB的显存（如RTX 3090）。
> * 由于ModelFun 会保存不同版本的数据和模型，方便用户快速检索和使用，所以可能会占用较大的存储空间，推荐使用500GB以上的存储。

## 2.2. 使用docker进行部署
当前 ModelFun 相关镜像已经上传至 Docker hub，可以直接拉取，实现一键安装部署。
您只需要下载[docker-compose文件](docker-compose-hub.yaml)，并使用如下命令即可完成部署。
```bash
docker compose -f docker-compose-hub.yaml up -d
```

另外，为了方便爱好者定制化开发部署，我们编制了详细的从代码编译镜像并进行构建的[指南](docs/modelfun镜像使用说明.md)

# 问题反馈和支持
如果您有遇到任何问题，欢迎从[Github Issue](https://github.com/CLUEbenchmark/modelfun/issues)联系我们

# 引用

如果你想在你的工作中提到ModelFun，请使用以下BibTeX条目。
```bibtex
@article{xu2021dataclue,
      title={DataCLUE: A Benchmark Suite for Data-centric NLP}, 
      author={Liang Xu and Jiacheng Liu and Xiang Pan and Xiaojing Lu and Xiaofeng Hou},
      year={2021},
      eprint={2111.08647},
      archivePrefix={arXiv},
      primaryClass={cs.CL}
}
```
