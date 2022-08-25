<template>
    <a-row :gutter="14">
        <a-col :span="13">
            <a-tabs type="capsule" :active-key="configKey" @change="e=>configKey=e" lazy-load>
                <a-tab-pane key="1" title="可视化配置" :disabled="!!addForm.id">
                    <a-form ref="formRef" :model="addForm" @submit="addRuleSubmit" class="rule-bg-1" auto-label-width>
                        <a-form-item field="ruleName" label="规则名称：" :validate-trigger="['change','input']" :rules="[{required:true,message:'规则名称不能为空'}]">
                            <a-input v-model="addForm.ruleName" placeholder="请输入规则名称" />
                        </a-form-item>
                        <a-form-item field="ruleType" label="规则类型：" :validate-trigger="['change','input']" :rules="[{required:true,message:'请选择规则类型'}]">

                            <template #label>
                                <span>规则类型：<a-popover>
                                        <icon-question-circle-fill class="blue" />
                                        <template #content>
                                            <ul>
                                                <li>内置模型，内置大模型辅助进行数据标注</li>
                                                <li>外部系统，通过api配置方式调用第三方系统，定义输入输出内容，进行数据标注</li>
                                                <li>模式匹配，选择不同类型的条件，包括关键词、范围匹配、字符串长度和词频，配置或组合不同的模板，进行自动化标注</li>
                                                <li>专家知识，上传一个分类词典文件</li>
                                                <li>查找数据库，调用第三方系统，定义好数据库查找的条件</li>
                                            </ul>
                                        </template>
                                    </a-popover></span>
                            </template>

                            <a-select v-model="addForm.ruleType" placeholder="请选择规则类型" :disabled="!!addForm.id">
                                <a-option :value="6">内置模型</a-option>
                                <a-option :value="4">外部系统</a-option>
                                <a-option :value="2">专家知识</a-option>
                                <a-option :value="3">查找数据库</a-option>
                                <a-option :value="1">模式匹配</a-option>
                            </a-select>
                        </a-form-item>
                        <a-form-item v-if="addForm.ruleType==2">
                            <a-button type="primary" :disabled="addForm.ruleType==2&&isExitParse" @click="uploadFunc">上传专家知识</a-button>
                        </a-form-item>
                        <a-form-item label="条件设置：" v-if="addForm.ruleType!='6'" required style="margin-bottom:0">
                        </a-form-item>
                        <!-- 模式匹配 -->
                        <template v-if="addForm.ruleType==1">
                            <div style="max-height:450px;overflow: auto;" id="scrolldIV">
                                <template v-for="(x,c) in orRuleList" :key='c'>
                                    <div v-if="c!=0" style="margin-bottom:5px">或：</div>
                                    <div class="rule-bg-2" style="padding-bottom:0">
                                        <a-form-item style="margin-bottom:0" :field="`rule&${c}&${index}`" v-for="(item,index) in x" :key="index" content-class="content-class" hide-label :validate-trigger="[]" :rules="[{validator:validatorRule(item)}]">
                                            <div class="rule-col">
                                                <a-space wrap>
                                                    <a-select v-model="item.ruleType" placeholder="请选择模式类型" style="width:120px">
                                                        <a-option value="0">关键词</a-option>
                                                        <a-option value="1">范围匹配</a-option>
                                                        <a-option value="2">字符串长度</a-option>
                                                        <a-option value="3">正则表达式</a-option>
                                                    </a-select>
                                                    <template v-if="item.ruleType=='0'">
                                                        <a-select v-model="item.include" placeholder="请选择模式" style="width:100px">
                                                            <a-option value="0">包含</a-option>
                                                            <a-option value="1">不包含</a-option>
                                                            <a-option value="2">==</a-option>
                                                        </a-select>
                                                        <a-input v-model="item.keyword" placeholder="请输入关键字" style="width:120px" />
                                                        <a-select v-if="item.include=='0'" v-model="item.countType" placeholder="请选择模式" style="width:120px">
                                                            <a-option value="0">词频设置</a-option>
                                                            <a-option value="1">==</a-option>
                                                            <a-option value="2">!=</a-option>
                                                            <a-option value="3">&gt;=</a-option>
                                                            <a-option value="4">&lt;=</a-option>
                                                            <a-option value="5">&gt;</a-option>
                                                            <a-option value="6">&lt;</a-option>
                                                        </a-select>
                                                        <a-input-number v-if="item.include=='0'&&item.countType!='0'" :min="0" v-model.number="item.count" placeholder="请输入词频" style="width:120px" />
                                                    </template>
                                                    <template v-if="item.ruleType=='1'">
                                                        <a-input v-model="item.keyword" placeholder="起始关键字" style="width:100px" />
                                                        <a-select v-model="item.gapType" placeholder="请选择模式" style="width:130px">
                                                            <a-option value="0">向右间隔</a-option>
                                                            <a-option value="1">间隔</a-option>
                                                            <a-option value="2">向右无限间隔</a-option>
                                                        </a-select>
                                                        <a-input-number :min="0" v-if="item.gapType!=='2'" v-model.number="item.gap" placeholder="请输入间隔数" style="width:120px" />
                                                        <a-input v-model="item.endKeyword" placeholder="结束关键字" style="width:100px" />

                                                    </template>
                                                    <template v-if="item.ruleType=='2'">
                                                        <a-select v-model="item.lenType" placeholder="请选择模式" style="width:130px">
                                                            <a-option value="0">==</a-option>
                                                            <a-option value="1">!=</a-option>
                                                            <a-option value="2">>=</a-option>
                                                            <a-option value="3">&lt;=</a-option>
                                                            <a-option value="4">></a-option>
                                                            <a-option value="5">&lt;</a-option>
                                                        </a-select>
                                                        <a-input-number :min="0" v-model.number="item.len" placeholder="请输入字符串长度" style="width:280px" />
                                                    </template>
                                                    <template v-if="item.ruleType=='3'">
                                                        <a-input v-model="item.regex" placeholder="请输入正则表达式" style="width:300px" />
                                                    </template>
                                                    <a-link @click="addRuleContent(x)" v-if="index== x.length-1">且</a-link>
                                                    <a-link @click="delRuleContent(x,index)" v-if="x.length>1">
                                                        <icon-delete />
                                                    </a-link>
                                                </a-space>
                                            </div>
                                        </a-form-item>
                                        <a-popconfirm content="确认要删除该条件吗?" @ok="delRuleList(c)">
                                            <icon-close v-show="orRuleList.length>1" class="close" />
                                        </a-popconfirm>
                                    </div>
                                </template>
                                <a ref="msg_end" name="1" href="#1"></a>
                            </div>

                            <a-button style="margin:0 0px 10px 15px" @click="addRuleList">添加 or 条件</a-button>
                            <a-form-item field="label" label=" 标记为：" :validate-trigger="['change','input']" :rules="[{required:true,message:'请选择标签'}]">
                                <a-select v-model="addForm.label" @search="handleSearch" allow-search :loading="loading" :filter-option="false" placeholder="请输入想要选择的标签">
                                    <a-option v-for="(item,index) in labelOptionsList" :key="index" :value="item.label">{{item.labelDes}}</a-option>
                                </a-select>
                            </a-form-item>
                        </template>

                        <!-- 专家知识 -->
                        <template v-if="addForm.ruleType=='2'">
                            <div class="rule-bg-2" style="padding-bottom:0" v-for="(item,index) in knowledgeList" :key="index">
                                <a-form-item :label="'专家知识'+(index+1)+'：'" style="margin-bottom:15px" field="expert" :rules="[{required:true,validator:validatorExpert(item)}]" :validate-trigger="['change','input']">
                                    <a-select v-model="item.value" placeholder="请选择专家知识" style="max-width:300px">
                                        <a-option v-for="(x,c) in expertList" :key="c" :value="x.id">{{x.fileName}}</a-option>
                                    </a-select>
                                    <!-- <a-button v-show="index!=0" type="text" status="danger" style="margin-left: auto" @click="delKnowledge(index)">删除</a-button> -->
                                </a-form-item>
                                <a-popconfirm content="确认要删除该条件吗?" @ok="delKnowledge(index)">
                                    <icon-close v-show="knowledgeList.length>1" class="close" />
                                </a-popconfirm>
                            </div>
                            <a-button style="margin:0 0px 10px 15px" @click="addKnowledge">添加 or 条件</a-button>

                        </template>

                        <!-- 查找数据库表单 -->
                        <div class="rule-bg-2" v-if="addForm.ruleType=='3'">
                            <a-form-item label="服务器地址：" field="host" :rules="[{required:true,message:'服务器地址不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.host" placeholder="请输入服务器地址" />
                            </a-form-item>
                            <a-form-item label="端口号：" field="port" :rules="[{required:true,message:'端口号不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.port" placeholder="请输入端口号" />
                            </a-form-item>
                            <a-form-item label="登录账号：" field="user" :rules="[{required:true,message:'登录账号不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.user" placeholder="请输入登录账号" />
                            </a-form-item>
                            <a-form-item label="登录密码：" field="password" :rules="[{required:true,message:'登录密码不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.password" placeholder="请输入登录密码" />
                            </a-form-item>
                            <a-form-item label="数据库类型：" field="databaseType" :rules="[{required:true,message:'数据库类型不能为空'}]" :validate-trigger="['change','input']">
                                <a-select v-model="addForm.databaseType" placeholder="请选择数据库类型">
                                    <a-option value="1">MySQL</a-option>
                                </a-select>
                            </a-form-item>
                            <a-form-item label="数据库名称：" field="database" :rules="[{required:true,message:'数据库名称不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.database" placeholder="请输入数据库名称" />
                            </a-form-item>
                            <a-form-item label="表名称：" field="table" :rules="[{required:true,message:'表名称不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.table" placeholder="请输入表名称" />
                            </a-form-item>
                            <a-form-item label="语料字段名：" field="sentenceColumn" :rules="[{required:true,message:'语料字段名不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.sentenceColumn" placeholder="请输入语料字段名" />
                            </a-form-item>
                            <a-form-item label="标签字段名：" field="labelColumn" :rules="[{required:true,message:'标签字段名不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.labelColumn" placeholder="请输入签字段名" />
                            </a-form-item>
                        </div>
                        <!-- 外部系统表单 -->
                        <div class="rule-bg-2" v-if="addForm.ruleType=='4'">
                            <a-form-item label="外部系统地址：" field="host" :rules="[{required:true,message:'外部系统地址不能为空'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.host" placeholder="请输入外部系统地址" />
                            </a-form-item>
                            <a-form-item field="batchSize" :rules="[{required:true,message:'单次调用可处理的语料数量不能为空'}]" :validate-trigger="['change','input']">
                                <template #label>
                                    Batch Size
                                    <a-tooltip content="调用外部系统接口时，单次调用可处理的语料数量。">
                                        <icon-question-circle-fill class="blue" />
                                    </a-tooltip>
                                </template>
                                <a-input-number :min="0" v-model="addForm.batchSize" placeholder="请输入单次调用可处理的语料数量" allow-clear />
                            </a-form-item>
                            <a-divider />
                            <a-form-item label="接口示例说明：">
                                <a-radio-group v-model="interfaceType" type="button">
                                    <a-radio value="in">输入示例</a-radio>
                                    <a-radio value="out">输出示例</a-radio>
                                </a-radio-group>
                            </a-form-item>
                            <a-form-item>
                                <codeView v-if="interfaceType=='in'" :value="codeText[2]" scene="look" />
                                <codeView v-else :value="codeText[0]" scene="look" />
                            </a-form-item>
                        </div>
                        <!-- GPT-3 -->
                        <template v-if="addForm.ruleType=='6'">
                            <a-form-item style="margin-bottom:15px" field="modelName">
                                <template #label>
                                    <span>模型类型：<a-popover>
                                            <icon-question-circle-fill class="blue" />
                                            <template #content>
                                                <ul>
                                                    <li>内容分析模型，适合有代表性的样本；</li>
                                                    <li>快速内容分析模型，适合有代表性的样本；</li>
                                                    <li>标签分析模型，适合有意义明确的标签名；</li>
                                                    <li>全能模型，速度较慢，不适合大量数据；</li>
                                                </ul>
                                            </template>
                                        </a-popover></span>
                                </template>
                                <a-select placeholder="请选择标签" v-model="addForm.modelName">
                                    <a-option :value="2">内容分析模型</a-option>
                                    <a-option :value="4">快速内容分析模型</a-option>
                                    <a-option :value="3">标签分析模型</a-option>
                                    <a-option :value="1">全能模型</a-option>
                                </a-select>
                            </a-form-item>
                            <a-form-item label="appKey：" v-if="addForm.modelName==1" style="margin-bottom:15px" field="appKey" :rules="[{required:true,message:'请输入appKey'}]" :validate-trigger="['change','input']">
                                <a-input v-model="addForm.appKey" placeholder="请输入appKey"></a-input>
                            </a-form-item>
                            <a-form-item label="标签类型：" style="margin-bottom:15px" field="labels" :rules="[{required:true,validator:validatorLabel}]" :validate-trigger="['change','input']">
                                <a-select placeholder="请选择标签" multiple v-model="addForm.labels" @change="change">
                                    <a-option v-for="(item,index) in labelSelectOptions" :key="index" :value="item.mapKey*1">{{item.mapValue}}</a-option>
                                </a-select>
                            </a-form-item>
                            <a-form-item label="标签说明示例：" style="margin-bottom:15px" field="sentence" :rules="[{required:true,validator:validatorData}]" :validate-trigger="['change','input']">
                                <a-space wrap direction="vertical" style="width:100%">
                                    <a-table :columns="columns" :data="data" style="flex:1" :pagination="false">
                                        <template #index="{ rowIndex }">
                                            {{rowIndex+1}}
                                        </template>
                                        <template #operation="{record,rowIndex}">
                                            <a-space>
                                                <a-button type="text" @click="edit(record,rowIndex)">编辑</a-button>
                                                <a-button type="text" status="danger" @click="del(rowIndex)" :disabled="canDel(record,rowIndex)">删除</a-button>
                                            </a-space>
                                            <!-- {{record}} -->
                                        </template>
                                    </a-table>
                                    <a-button type="text" @click="add">
                                        +添加示例
                                    </a-button>
                                </a-space>
                            </a-form-item>
                        </template>

                        <a-form-item>
                            <a-space>
                                <a-button html-type="submit" type="primary" :disabled="addForm.ruleType=='2'&&isExitParse">保存</a-button>
                                <a-popover>
                                    <icon-question-circle-fill class="blue" v-if="addForm.ruleType=='6'" />
                                    <template #content>
                                        <ul>
                                            <li>单次仅支持运行一个内置模型规则，一条运行完成后才支持构建下一个内置模型规则；</li>
                                            <li>内置模型规则与其他类型规则构建不冲突，运行内置模型规则时，可支持构建运行其他类型的规则；</li>
                                            <li>内置模型：平台内置一个分类模型，对上传的未标注语料进行标注；</li>
                                        </ul>
                                    </template>
                                </a-popover>
                                <a-button @click="addFormGoBack ">返回</a-button>
                            </a-space>
                        </a-form-item>
                        <template v-if="addForm.ruleType=='6'||addForm.ruleType=='1'||addForm.ruleType=='4'">
                            <a-divider />
                            <a-form-item label="规则测试：">
                            </a-form-item>
                            <a-form-item label="测试文本：">
                                <a-input v-model="testText" placeholder="请输入测试文本" />
                            </a-form-item>
                            <a-form-item>
                                <a-button type="primary" @click="testGpt">RUN</a-button>
                            </a-form-item>
                            <a-form-item label="测试结果：">
                                <a-textarea :auto-size="{minRows: 4, maxRows: 8 }" v-model="testResult" placeholder="请输入方法体" />
                            </a-form-item>
                        </template>
                    </a-form>
                </a-tab-pane>
                <a-tab-pane key="2" title="代码编写" :disabled="!!addForm.id" style="z-index: 5;">
                    <!-- 查看标签集 -->
                    <a-link style="float:right;margin-right:10px" @click="checkLabel">标签集查看</a-link>
                    <a-form ref="formRef" :model="addForm" @submit="addRuleSubmit" class="rule-bg-1" auto-label-width>
                        <!-- 代码编写表单 -->
                        <div class="rule-bg-2">
                            <a-form-item field="ruleName" label="规则名称：" :validate-trigger="['change','input']" :rules="[{required:true,message:'规则名称不能为空'}]">
                                <a-input v-model="addForm.ruleName" placeholder="请输入规则名称" />
                            </a-form-item>
                            <a-form-item field="functionName" label="方法名：" :validate-trigger="['change','input']" :rules="[{required:true,validator:validatorFuncName}]">
                                <a-input v-model.trim="addForm.functionName" @input="addForm.functionName=addForm.functionName.replace(/[\s]/g,'')" placeholder="请输入方法名" />
                            </a-form-item>
                            <a-form-item label="方法体：" field="functionBody" :rules="[{required:true,message:'方法体不能为空'}]" :validate-trigger="['change','input']">
                                <codeView :value="addForm.functionBody" scene="edit" @update:value="e=>addForm.functionBody=e" />
                            </a-form-item>
                        </div>
                        <a-form-item>
                            <a-space>
                                <a-button html-type="submit" type="primary" :disabled="addForm.ruleType=='2'&&isExitParse">保存</a-button>
                                <a-tooltip background-color="#fff" content-class="contentClass">
                                    <icon-question-circle-fill class="blue" />
                                    <template #content>
                                        <div style="width:530px">
                                            <codeView :value="codeText[1]" scene="look" />
                                        </div>
                                    </template>
                                </a-tooltip>
                                <span></span>
                                <a-button @click="addFormGoBack ">返回</a-button>
                            </a-space>
                        </a-form-item>
                        <a-divider />
                        <a-form-item label="规则测试：">
                        </a-form-item>
                        <a-form-item label="测试文本：">
                            <a-input v-model="testText" placeholder="请输入测试文本" />
                        </a-form-item>
                        <a-form-item>
                            <a-button type="primary" @click="testFunc">RUN</a-button>
                        </a-form-item>
                        <a-form-item label="测试结果：">
                            <a-textarea :auto-size="{minRows: 4, maxRows: 8 }" v-model="testResult" placeholder="请输入方法体" />
                        </a-form-item>
                    </a-form>
                    <labelList ref="labelListRef" />
                </a-tab-pane>
            </a-tabs>
        </a-col>
        <a-col :span="11">
            <unlabeledData ref="unlabeledDataRef" :ruleType="addForm.ruleType" :label="addForm.label" :ruleId="addForm.id" />
        </a-col>
    </a-row>
    <a-modal v-model:visible="visible" title-align='start' @ok="handleOk" @cancel="visible=false" ok-text='保存'>
        <template #title>标签说明示例</template>
        <a-form :model='form' auto-label-width>
            <a-form-item label="标签：">
                <a-select placeholder="请选择标签" v-model="form.labelId" :disabled="canDel(form,form.index)">
                    <a-option v-for="(item,index) in getValue " :key="index" :value="item.labelId">{{item.label}}</a-option>
                </a-select>
            </a-form-item>
            <a-form-item label="标签说明示例：">
                <a-input v-model="form.sentence" placeholder="请输入标签说明示例"></a-input>
            </a-form-item>
        </a-form>
    </a-modal>
    <knowledge ref="knowledgeRef" @updateOk="getKnowledgeProgress" />
</template>
<script setup>
import unlabeledData from "./unlabeledData.vue";
import knowledge from "./knowledge.vue";
import labelList from "./label-list.vue";
import dayjs from "dayjs";
import ruleOptions from "./rule";
import { ref, nextTick, computed } from "vue";
import { Message } from "@arco-design/web-vue";
import { useRoute } from "vue-router";
import { useDebounceFn } from "@vueuse/core";
import useLoading from "@/hooks/loading";
import { sysFunc, keyWord } from "./rule";
import { toFixed } from "@/utils";
import {
    ruleAdd,
    ruleUpdate,
    getExpertList,
    testRule,
    getLabelOptions,
    getExpertRunning,
    gtpRuleFunc,
    regRuleFunc,
    openapiRuleFunc,
} from "@/api/task/text/ruleSet.js";
const unlabeledDataRef = ref(null);
const { loading, setLoading } = useLoading();
const router = useRoute();
const taskId = ref(router.params.id);
const taskName = ref(router.params.name);
let body = ['  label = 2 if "人" in x else -1', "  return label"];
const addForm = ref({
    ruleType: 6,
    functionBody: body.join("\n"),
    labels: [],
    modelName: 2,
    appKey: "",
});
const labelSelectOptions = ref([]);
const configKey = ref("1");
const formRef = ref(null);
const form = ref({});
const total = ref(0);
const labelOptionsList = ref([]);
const knowledgeRef = ref(null);
const visible = ref(false);
const knowledgeList = ref([
    {
        value: "",
    },
]);
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        slotName: "index",
        width: "80",
    },
    {
        title: "标签说明示例",
        dataIndex: "sentence",
    },
    {
        width: "130",
        title: "标签",
        dataIndex: "labelDes",
    },
    {
        width: "80",
        title: "操作",
        slotName: "operation",
    },
]);
let obj = {
    code: 0, // 0表示调用成功，非0表示调用失败
    msg: "调用成功", // 如果调用失败，返回失败原因
    data: [
        {
            sentence: "查一下什么时候?", // 语料内容
            labelId: 111, // 标签ID
            labelDes: "买家咨询物流信息", // 标签描述
        },
        {
            sentence: "呵呵，再送一个呗?",
            labelId: 86,
            labelDes: "买家咨询商品是否有赠品",
        },
    ],
};
var result = JSON.stringify(obj, null, 4); //格式化后的json字符串形式
let arr = result.split("\n");
arr[1] = arr[1] + "// 0表示调用成功，非0表示调用失败";
arr[2] = arr[2] + "// 如果调用失败，返回失败原因";
arr[5] = arr[5] + "// 语料内容";
arr[6] = arr[6] + "// 标签ID";
arr[7] = arr[7] + "// 标签描述";
let pythonArr = [
    "# 得到标签",
    'label=1 if "优惠券" in x else -1',
    "# 返回标签",
    "return label",
    "",
    "",
    "#正则匹配:输入是文本，输出是一个标签.pattern_regular为方法名，可以改动",
    "import re",
    "# 得到标签",
    'x = re.search("^优惠.*券$", x)',
    "if x:",
    "   return 1",
    "else:",
    "   return -1",
];
let inObj = {
    sentences: ["你好呀", "今天天气怎么样"],
};
let inarr = JSON.stringify(inObj, null, 4).split("\n");
inarr[4] = "          #...\n    ]";
const codeText = ref([arr.join("\n"), pythonArr.join("\n"), inarr.join("\n")]);
const orRuleList = ref([
    [
        {
            ruleType: "0",
            contentValue: "0",
            keyword: "",
            countType: "0",
            gapType: "0",
            include: "0",
            lenType: "0",
            len: null,
        },
    ],
]);
const testResult = ref("");
const state = ref({});
const beginTime = ref(dayjs().format("YYYY-MM-DD HH:mm:ss"));
const data = ref([]);
const testText = ref("");
const getValue = computed(() => {
    let arr = addForm.value.labels.map((x) => {
        console.log(labelSelectOptions.value.find((c) => c.mapKey == x + ""));
        return {
            label: labelSelectOptions.value.find((c) => c.mapKey == x + "")
                ?.mapValue,
            labelId: x,
        };
    });
    return arr;
});
const addFormGoBack = () => {
    emit("goBack");
};
const handleOk = (done) => {
    if (form.value.index != undefined) {
        data.value[form.value.index].labelId = form.value.labelId;
        data.value[form.value.index].sentence = form.value.sentence;
        (data.value[form.value.index].labelDes = labelSelectOptions.value.find(
            (x) => x.mapKey == form.value.labelId
        ).mapValue),
            (visible.value = false);
    } else {
        if (form.value.labelId == undefined) {
            Message.error("请选择标签！");
            done(false);
            return;
        }
        data.value.push({
            sentence: form.value.sentence,
            labelId: form.value.labelId,
            labelDes: labelSelectOptions.value.find(
                (x) => x.mapKey == form.value.labelId
            ).mapValue,
        });
        visible.value = false;
    }
};
const del = (index) => {
    data.value.splice(index, 1);
};
const add = () => {
    visible.value = true;
    form.value = {};
    console.log(addForm.value.labels);
    if (addForm.value.labels.length == 1) {
        form.value.labelId = addForm.value.labels[0];
    }
};
const edit = (val, index) => {
    console.log(val);
    visible.value = true;
    form.value = val;
    form.value.index = index;
};
const canDel = computed(() => {
    return (val, i) => {
        if (i == undefined) return false;
        let arr = data.value.filter((x) => x.labelId == val.labelId);
        return arr.length <= 1;
    };
});
const change = (e) => {
    e.map((val) => {
        let flag = data.value.find((item) => item.labelId == val);

        if (!flag) {
            data.value.push({
                sentence: "",
                labelId: val,
                labelDes: labelSelectOptions.value.find((x) => x.mapKey == val)
                    .mapValue,
            });
        }
    });
    data.value = data.value.filter((val) => {
        let obj = e.find((item) => item == val.labelId);
        console.log(obj);
        return obj || obj === 0;
    });
};
const addRuleSubmit = async ({ errors, values }) => {
    if (!errors) {
        let obj = { ...values };
        if (configKey.value == "2") {
            obj = {
                functionName: values.functionName,
                functionBody: values.functionBody,
            };
        } else if (addForm.value.ruleType == "2") {
            let arr = knowledgeList.value.map((x) => {
                let obj = expertList.value.find((y) => y.id == x.value);
                return {
                    address: obj.address,
                    id: obj.id,
                };
            });
            obj = arr;
        } else if (addForm.value.ruleType == "1") {
            let arr = orRuleList.value.map((x) => {
                return x.map((y) => {
                    if (y.ruleType == "1") {
                        y.regex = ruleOptions.range["&" + y.gapType](
                            y.gap,
                            y.keyword,
                            y.endKeyword
                        );
                    } else if (y.ruleType == "2") {
                        y.regex = ruleOptions.len["&" + y.lenType](y.len);
                    }
                    return y;
                });
            });
            obj = arr;
        } else if (addForm.value.ruleType == "6") {
            obj = {
                example: data.value,
                labels: addForm.value.labels,
                modelName: addForm.value.modelName,
                appKey:
                    addForm.value.modelName == 1 ? values.appKey : undefined,
            };
        }
        let labelobj = {};
        if (configKey.value == "1" && addForm.value.ruleType == "1") {
            labelobj = {
                label: values.label,
                labelDes: labelOptionsList.value.find(
                    (x) => x.label == values.label
                ).labelDes,
            };
        }
        if (values.id) {
            let res = await ruleUpdate({
                taskId: taskId.value,
                ruleId: values.id,
                ruleName: values.ruleName,
                metadata: JSON.stringify(obj),
                ruleType: configKey.value == "2" ? "5" : values.ruleType,
                ...labelobj,
                updateStartTime: beginTime.value,
                updateEndTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            });
            Message.success("修改成功");
        } else {
            let res = await ruleAdd({
                taskId: taskId.value,
                ruleName: values.ruleName,
                metadata: JSON.stringify(obj),
                ruleType: configKey.value == "2" ? "5" : values.ruleType,
                ...labelobj,
                appKey:
                    addForm.value.modelName == 1 ? values.appKey : undefined,
                createStartTime: beginTime.value,
                createEndTime: dayjs().format("YYYY-MM-DD HH:mm:ss"),
            });
            Message.success("添加成功");
        }
        beginTime.value = "";
        formRef.value.resetFields();
        emit("addOk");
    }
};
const uploadFunc = () => {
    knowledgeRef.value.init(addForm.value.id);
};
const addKnowledge = () => {
    knowledgeList.value.push({
        value: "",
    });
};
const delKnowledge = (index) => {
    knowledgeList.value.splice(index, 1);
};
const getExpertListFunc = async () => {
    let res = await getExpertList({
        taskId: taskId.value,
    });
    expertList.value = res.data;
};
const editRule = (record) => {
    let metadata = JSON.parse(record.metadata);
    addForm.value.id = record.id;
    addForm.value.ruleName = record.ruleName;
    addForm.value.ruleType = record.ruleType;

    configKey.value = "1";
    if (record.ruleType == 2) {
        let arr = JSON.parse(record.metadata);
        knowledgeList.value = arr.map((x) => {
            return {
                value: x.id,
            };
        });
    } else if (record.ruleType == 5) {
        addForm.value = {
            ...addForm.value,
            ...metadata,
        };
        configKey.value = "2";
        testText.value = "";
        testResult.value = "";
    } else if (record.ruleType == 1) {
        addForm.value.label = record.label;
        addForm.value.labelDes = record.labelDes;
        orRuleList.value = metadata;
    } else if (record.ruleType == 6) {
        addForm.value.labels = metadata.labels;
        addForm.value.modelName = metadata.modelName;
        addForm.value.appKey = metadata.appKey;
        data.value = metadata.example;
    } else {
        addForm.value = {
            ...addForm.value,
            ...metadata,
        };
    }
    beginTime.value = dayjs().format("YYYY-MM-DD HH:mm:ss");
};

const getKnowledgeProgress = async () => {
    let res = await getExpertRunning({
        taskId: taskId.value,
    });
    if (!res.data.exitParseTask) {
        isExitParse.value = false;
        Message.success("上传的专家知识解析成功！");
        knowledgeList.value = [
            {
                value: "",
            },
        ];
        getExpertListFunc();
    } else {
        isExitParse.value = true;
        timer.value = setTimeout(() => {
            getKnowledgeProgress();
        }, 5000);
    }
};
const timer = ref("");
const isExitParse = ref(false);
const addRuleContent = (item) => {
    unlabeledDataRef.value.init(orRuleList.value);
    item.push({
        ruleType: "0",
        contentValue: "0",
        keyword: "",
        countType: "0",
        gapType: "0",
        include: "0",
        lenType: "0",
        len: null,
    });
};
const delRuleContent = (item, index) => {
    unlabeledDataRef.value.init(orRuleList.value);
    item.splice(index, 1);
};
const delRuleList = (index) => {
    unlabeledDataRef.value.init(orRuleList.value);
    orRuleList.value.splice(index, 1);
};
const msg_end = ref(null);
const addRuleList = () => {
    unlabeledDataRef.value.init(orRuleList.value);
    orRuleList.value.push([
        {
            ruleType: "0",
            contentValue: "0",
            keyword: "",
            countType: "0",
            gapType: "0",
            include: "0",
            lenType: "0",
            len: "",
        },
    ]);
    // msg_end.value.click();
    nextTick(()=>{
        var div = document.getElementById("scrolldIV");
    div.scrollTop = div.scrollHeight;
    })
    
};
const expertList = ref([]);
const validatorFuncName = (value, callback) => {
    let reg = /^([a-zA-Z_$])([^\x00-\xff]|[a-zA-Z0-9_$])*$/;
    if (!value) {
        callback("请输入方法名");
    } else if (!reg.test(value)) {
        callback("方法名只能包含字母、数字、下划线，并以字母开头");
    } else if (keyWord.find((x) => x == value)) {
        callback("方法名不能为系统关键字");
    } else if (sysFunc.find((x) => x == value)) {
        callback("方法名不能为系统内置函数");
    } else {
        callback();
    }
};
const validatorData = (value, callback) => {
    let flag = data.value.find((x) => !x.sentence);

    if (flag) {
        callback("请完善标签说明示例!");
    } else {
        callback();
    }
};
const validatorLabel = (value, callback) => {
    if (addForm.value.labels.length > 0) {
        if (addForm.value.modelName == 2 && addForm.value.labels.length == 1) {
            callback("请选择至少两个类别");
        } else {
            callback();
        }
    } else {
        callback("请选择标签类型");
    }
};
const validatorRule = (item) => {
    return (value, callback) => {
        if (item.ruleType == "0") {
            if (!item.keyword) {
                callback("请输入关键字");
            } else if (item.countType != "0" && !item.count) {
                callback("请输入词频");
            } else {
                callback();
            }
        } else if (item.ruleType == "1") {
            if (!item.keyword) {
                callback("请输入起始关键字");
            } else if (!item.endKeyword) {
                callback("请输入结束关键字");
            } else {
                if (item.gapType != "2" && !item.gap) {
                    callback("请输入间隔数");
                } else {
                    callback();
                }
            }
        } else if (item.ruleType == "2") {
            if (!item.len) {
                callback("请输入字符串长度");
            } else {
                callback();
            }
        } else {
            if (!item.regex) {
                callback("请输入正则表达式");
            } else {
                callback();
            }
        }
    };
};
const testGpt = async () => {
    formRef.value.validate(async (validate) => {
        if (!validate) {
            if (addForm.value.ruleType == "1") {
                let arr = orRuleList.value.map((x) => {
                    return x.map((y) => {
                        if (y.ruleType == "1") {
                            y.regex = ruleOptions.range["&" + y.gapType](
                                y.gap,
                                y.keyword,
                                y.endKeyword
                            );
                        } else if (y.ruleType == "2") {
                            y.regex = ruleOptions.len["&" + y.lenType](y.len);
                        }
                        return y;
                    });
                });
                let res = await regRuleFunc({
                    text: testText.value,
                    rules: arr,
                    labelDesc: labelOptionsList.value.find(
                        (x) => x.label == addForm.value.label
                    ).labelDes,
                });
                testResult.value = res.data;
            } else if (addForm.value.ruleType == "4") {
                let res = await openapiRuleFunc({
                    taskId: taskId.value,
                    host: addForm.value.host,
                    requestBody: testText.value,
                    batchSize: 0,
                });
                testResult.value = res.data;
            } else {
                let res = await gtpRuleFunc({
                    taskId: taskId.value,
                    ruleName: addForm.value.ruleName,
                    texts: testText.value,
                    example: data.value,
                    labels: addForm.value.labels,
                    modelName: addForm.value.modelName,
                    appKey: addForm.value.appKey,
                });
                testResult.value = res.data;
            }
        }
    });
};
const testFunc = async () => {
    let obj = {
        testText: "请输入测试文本！",
        functionName: "请输入方法名！",
        functionBody: "请输入方法体！",
    };
    for (const key in obj) {
        if (key == "testText" ? !testText.value : !addForm.value[key]) {
            const element = obj[key];
            Message.error(obj[key]);
            return;
        }
    }
    let res = await testRule({
        functionName: addForm.value.functionName,
        functionBody: addForm.value.functionBody,
        example: testText.value,
    });
    testResult.value = res.data;
};
const validatorExpert = (item) => {
    return (value, callback) => {
        if (!item.value) callback("请选择专家知识");
        else callback();
    };
};
const getLabelOptionsList = (val) => {
    return getLabelOptions({
        taskId: taskId.value,
        labelDesc: val,
        pageNum: 1,
        pageSize: 1000,
    });
};
const handleSearch = (value, val) => {
    useDebounceFn(async () => {
        setLoading(true);
        try {
            let res = await getLabelOptionsList(value);
            if (val) {
                labelSelectOptions.value = res.data;
            }
            labelOptionsList.value = res.data.map((x) => {
                return {
                    label: x.mapKey * 1,
                    labelDes: x.mapValue,
                };
            });
            let obj = labelOptionsList.value.find(
                (x) => x.label == addForm.value.label
            );
            if (!obj && addForm.value.label) {
                labelOptionsList.value.unshift({
                    label: addForm.value.label * 1,
                    labelDes: addForm.value.labelDes,
                });
            }
        } catch (error) {
        } finally {
            setLoading(false);
        }
    }, 500)();
};

const kownTimer = ref("");
const labelListRef = ref(null);
const checkLabel = () => {
    labelListRef.value.init();
};
const interfaceType = ref("in");

const emit = defineEmits(["addOk", "goBack"]);
const props = defineProps(["record"]);
if (props.record?.id) {
    editRule(props.record);
}
handleSearch("", 1);
getExpertListFunc();
setTimeout(() => {
    unlabeledDataRef.value.init(orRuleList.value, 1);
});
</script>
<style lang='less' scoped>
.rule-bg-1 {
    padding: 10px 10px 10px 0;
    background: var(--color-fill-0);
    // background: red;
}
.rule-bg-2 {
    margin: 0px 0px 10px 15px;
    padding: 10px 10px 15px 0;
    background: var(--color-fill-3);
    position: relative;
    .close {
        position: absolute;
        right: 5px;
        top: 5px;
        cursor: pointer;
    }
}
.rule-col {
    padding: 0 10px;
    width: 100%;
    & + .rule-col {
        margin-top: 10px;
    }
}
:deep(.content-class) {
    flex-direction: column !important;
}

:deep(.arco-divider-horizontal) {
    border-bottom: 1px solid var(--color-neutral-4);
}
.blue {
    color: rgb(var(--blue-6));
    cursor: pointer;
}
</style>
