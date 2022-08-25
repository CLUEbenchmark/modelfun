# 一键标注
![一键标注](.\images\一键标注.png)

# Label Function规则格式文档

当前的Label Function（下面简称lf）一共有6中类型的规则：

1. 模式匹配，即正则表达式
2. 专家知识
3. 查找数据库
4. 外部系统
5. 代码编写
6. 内置模型

## 统一格式

由于每种规则的表达方式都各不相同，但是在接口上需要统一，所以对规则进行了格式的统一，把相同的字段进行了抽取，把不同的字段放入metadata字段中，以json字符串的形式保存。

总的格式如下：

```json
{
  "taskId": 1, // 必填
  "ruleName": "规则名称", // 必填
  "ruleType": "1",   // 规则类型，1：模式匹配，2：专家知识，3：查找数据库，4：外部系统，5：代码编写，6：内置模型，  必填
  "metadata": "", // 规则内容，这是一个json格式的字符串，不同的规则类型对应的规则内容不同   必填
  "label": "1", // 标签的ID    只有当ruleType为1的情况下才必填
  "labelDes": "规则描述" // 标签的描述   只有当ruleType为1的情况下才必填
}
```

### 模式匹配

模式匹配的metadata格式如下：

```json
[
  [
    {
      "ruleType": "1", // 0：关键词，1：范围匹配，2：字符串长度，3：正则表达式
      "keyword": "关键词", // 该字段只有在规则类型为0或者1的情况下才有用
      "include": "0", // 关键词包含类型。0：包含，1：不包含，2：==(表示完全相等)
      "countType": "1", // 词频类型。0：不设置词频，1：==，2：!=，3：>=，4：<=，5：>，6：<。该字段只有在ruleType为0并且include为0时才有效
      "count": "1", // 词频，即关键词出现的次数，该字段只有在countType字段生效的情况下才生效
      "endKeyword": "结束关键词", // 结束关键词，该字段只有在ruleType=1的情况下才生效
      "gapType": "1", // 间隔类型，范围匹配间隔类型。0：向前间隔，1：间隔，2：向右无限间隔。该字段只有在ruleType=1的情况下才生效
      "gap": "1", // 间隔数量。该字段只有在gapType生效的情况下才生效
      "lenType": "0", // 字符串长度类型。0：==，1：!=，2：>=，3：<=，4：>，5：< 该字段只有在ruleType为3的情况下才生效
      "len": 0, // 字符串长度。该字段只有在lenType生效的情况下才生效
      "regex": ".*你好.*" // 正则表达式，当ruleType=1的时候不提供，其他情况下都需要拼装
    },
    {
      //内层的list，表示多个规则的且关系
    }
  ],
  [
    // 外层的list，表示多个规则的或关系
  ]
]
```

示例：

![image-20220531114336180](.\images\模式匹配示例.png)

如图所示的规则，其metadata为：

```json
[
  [
    {
      "ruleType": "0",
      "contentValue": "0",
      "keyword": "教育",
      "countType": "1",
      "gapType": "0",
      "include": "0",
      "lenType": "0",
      "count": 1
    },
    {
      "ruleType": "1",
      "contentValue": "0",
      "keyword": "大学",
      "countType": "0",
      "gapType": "0",
      "include": "0",
      "lenType": "0",
      "gap": 1,
      "endKeyword": "生",
      "regex": "大学.{1}生"
    }
  ],
  [
    {
      "ruleType": "2",
      "contentValue": "0",
      "keyword": "",
      "countType": "0",
      "gapType": "0",
      "include": "0",
      "lenType": "0",
      "len": 10,
      "regex": "^.{10}$"
    }
  ],
  [
    {
      "ruleType": "3",
      "contentValue": "0",
      "keyword": "",
      "countType": "0",
      "gapType": "0",
      "include": "0",
      "lenType": "0",
      "regex": ".*大学.*"
    }
  ]
]
```

**推荐生成模式匹配规则的时候，使用ruleType=3（即正则表达式）类型的规则，比较简单。单条正则表达式规则如下**

```json
[
  [
    {
      "ruleType": "3",
      "contentValue": "0",
      "keyword": "",
      "countType": "0",
      "gapType": "0",
      "include": "0",
      "lenType": "0",
      "len": "",
      "regex": ".*大学.*"
    }
  ]
]
```

### 专家知识

专家知识metadata如下：

```json
[
  {
    "address": "专家知识文件地址", // oss路径
    "fileName": "文件名",
    "id": 1 // 专家知识ID
  },
  {
      // 每个json对象表示一个专家知识，多个专家知识之间是或的关系
  }
]
```

由于专家知识需要先上传文件，所以不建议生成该规则

### 查找数据库

```json
{
    "host": "", // 地址
    "port": 3306, // 端口号
    "user": "", //用户名
    "password": "", //密码
    "databaseType": 1, //数据库类型，1：MySQL目前只支持mysql
    "database": "", //数据库名
    "table": "", // 表名
    "sentenceColumn": "",  // 语料字段名
    "labelColumn": "label" //标签字段名
}
```

### 外部系统

```json
{
    "host": "http:127.0.0.1:8081/test" // 接口地址
    "batchSize": 128     // 每次调用接口批次大小
}
```

### 代码编写

```json
{
    "functionName": "test_lf", //方法名
    "functionBody": " return 1" // 方法体
}
```

### 内置模型

```json
{
  "example": [ // 标签示例说明
    {
      "sentence": "必须五星，这种震人心脾的感觉只有森海有了，什么索尼大法都是渣渣。很棒",
      "labelId": 1,
      "labelDes": "Positive"
    },
    {
      "sentence": "差劲，一摔就掉，内屏都给我摔碎了",
      "labelId": 2,
      "labelDes": "Negative"
    },
    {
      "sentence": "还不错，等试用一段时间再说",
      "labelId": 1,
      "labelDes": "Positive"
    }
  ],
  "labels": [ // 选择的标签
    1,
    2
  ],
  "modelName": 3 // 模型类型，1：gpt3，2：sim，3：roberta 4：clustering
}
```

该规则示例：

![image-20220531145450782](.\images\内置模型示例.png)

**内置模型完整示例**

```json
{
  "taskId": 1, // 必填
  "ruleName": "规则名称", // 必填
  "ruleType": "6",   // 规则类型，1：模式匹配，2：专家知识，3：查找数据库，4：外部系统，5：代码编写，6：内置模型，  必填
  "metadata": "{\"example\":[{\"sentence\":\"必须五星，这种震人心脾的感觉只有森海有了，什么索尼大法都是渣渣。很棒\",\"labelId\":1,\"labelDes\":\"Positive\"},{\"sentence\":\"差劲，一摔就掉，内屏都给我摔碎了\",\"labelId\":2,\"labelDes\":\"Negative\"},{\"sentence\":\"还不错，等试用一段时间再说\",\"labelId\":1,\"labelDes\":\"Positive\"}],\"labels\":[1,2],\"modelName\":3}" 
}
```

