## 概览

本项目是modelfun项目的算法部分，主要实现了对开源大模型和弱监督聚合方法的数据自动标注功能。


该项目实现的主要功能为：

1. 本地和在线文件的读取和解析。
2. 用户自定义代码的执行。
3. 基于大模型的标记函数生成。
4. 弱标签的聚合和模型保存。
5. 基于聚合标签的分类模型训练。
6. NER模型预标注。
7. 模型部署。
8. 相关评价指标的计算。

具体信息可以查看两个子项目（<a href="https://github.com/CLUEbenchmark/modelfun/tree/main/modelfun/algorithm/torch_backend">torch_backend</a> 和 <a href="https://github.com/CLUEbenchmark/modelfun/tree/main/modelfun/algorithm/paddle_backend">paddle_backend</a> 的内容）
