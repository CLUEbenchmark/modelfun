<template>
    <template v-if="!isAdd">
        <a-space direction="vertical" fill>
            <a-alert :show-icon="false">
                <a-row align="center">
                    <a-col flex="none">标注概览（总）</a-col>
                    <a-col :flex="1">
                        <div class="row">
                            <div>
                                训练集
                            </div>
                            <div class="flex2">
                                验证集
                                <a-popover>
                                    <icon-question-circle-fill />
                                    <template #content>
                                        <ul>
                                            <li>该测试集统计结果是基于切分出来进行展示的测试集进行统计；</li>
                                            <li>计算方式：
                                                <ul>
                                                    <li>覆盖率：经过投票后被标注的测试集语料量 / 切分出来展示的测试集数量</li>
                                                    <li>准确率：经过投票后和测试集标签一致的语料量 / 切分出来展示的测试集数量</li>
                                                </ul>
                                            </li>
                                        </ul>
                                    </template>
                                </a-popover>
                            </div>
                            <div class="flex2">
                                未标注数据集
                            </div>
                        </div>
                        <div class="row">
                            <div>
                                标签类别：{{state.labelCount||'0'}} 类
                            </div>
                            <div>
                                覆盖率：{{state.testDataCoverage||'0.00'}}%
                            </div>
                            <div>
                                准确率：{{state.accuracy||'0.00'}}%
                            </div>
                            <div>
                                覆盖率：{{state.coverage||'0.00'}}%
                            </div>
                            <div>
                                冲突率：{{state.conflict||'0.00'}}%
                            </div>
                        </div>
                    </a-col>
                </a-row>
            </a-alert>
            <a-space>
                <a-button type="primary" size="large" @click="addRuleFunc">新增标注规则</a-button>
                <a-button type="primary" size="large" @click="startIntegration" :loading="isRunning">{{isRunning?'正在集成':'开始集成'}}</a-button>

            </a-space>
            <a-table :columns="columns" :data="data" :pagination='false' :bordered="{headerCell:true}">
                <template #rowIndex="{ rowIndex }">
                    {{rowIndex+1}}
                </template>
                <template #accuracy="{ record }">
                    <span v-if="record.completed==0">-</span>
                    <a-popover title="提示" v-else-if="record.accuracy<accuracy">
                        <span class="red">
                            <icon-exclamation-circle-fill style="margin-right:5px" />{{record.accuracy? `${toFixed(record.accuracy)}%`: "-"}}
                        </span>
                        <template #content>
                            <p>当前 准确率 为：<span class="red">{{record.accuracy? `${toFixed(record.accuracy)}%`: "-"}}</span></p>
                            <p>建议值：<span class="red">{{accuracy}}%</span></p>
                            <p>优化建议：请修改标注规则提升 准确率的结果，保证标注结果的数据质量；</p>
                        </template>
                    </a-popover>
                    <span v-else>{{record.accuracy? `${toFixed(record.accuracy)}%`: "-"}}</span>

                </template>
                <template #coverage="{ record }">
                    <span>{{isRunningFunc(record,record.coverage? `${toFixed(record.coverage)}%`: "-") }}</span>
                </template>
                <template #completed="{ record }">
                    <span :class="{green:record.completed==1,red:record.completed==2||record.completed==3,yellow:record.completed==0}">{{["运行中", "运行完成", "运行出错",'删除中'][record.completed*1]}}</span>
                </template>
                <template #operation="{ record }">
                    <a-button type="text" style="padding:5px" @click="editRule(record)" :disabled="record.completed==0||record.completed==3">编辑</a-button>
                    <a-popconfirm :content="`确认删除 ${record.ruleName} 吗？`" @ok="deleteFunc(record)">
                        <a-button type="text" style="padding:5px" :disabled="record.completed==3||record.completed==0
                        " @click.stop> 删除</a-button>
                    </a-popconfirm>
                </template>
            </a-table>
        </a-space>
    </template>

    <add-rule v-else @addOk="addOk" @goBack="isAdd=false" :record="recordObj"></add-rule>
    <a-modal v-model:visible="visible" title-align='start' @before-ok="handleOk" @cancel="visible=false" ok-text='开始集成' width="800px">
        <template #title>开始集成任务</template>
        <p>
            目前有 <span class="red big">{{showUnGoodData.length}}条</span> 标注规则的准确率和覆盖率低于建议值，会严重影响训练集生成的质量，请问确认继续吗？
        </p>
        <a-table :columns="modelColumns" :data="showUnGoodData" :pagination='false' :bordered="{headerCell:true}">
            <template #rowIndex="{ rowIndex }">
                {{rowIndex+1}}
            </template>
            <template #accuracy="{ record }">
                <a-popover title="提示" v-if="record.accuracy<accuracy">
                    <span class="red">
                        <icon-exclamation-circle-fill style="margin-right:5px" />{{record.accuracy? `${toFixed(record.accuracy)}%`: "-"}}
                    </span>
                    <template #content>
                        <p>当前 准确率 为：<span class="red">{{record.accuracy? `${toFixed(record.accuracy)}%`: "-"}}</span></p>
                        <p>建议值：<span class="red">{{accuracy}}%</span></p>
                        <p>优化建议：请修改标注规则提升 准确率的结果，保证标注结果的数据质量；</p>
                    </template>
                </a-popover>
                <span v-else>{{record.accuracy? `${toFixed(record.accuracy)}%`: "-"}}</span>

            </template>
            <template #coverage="{ record }">
                <span>{{record.coverage? `${toFixed(record.coverage)}%`: "-"}}</span>

            </template>
            <template #operation="{ record }">
                <a-button type="text" style="padding:5px" @click="popEditRule(record)" :disabled="record.completed==0||record.completed==3">编辑</a-button>
                <a-popconfirm :content="`确认删除 ${record.ruleName} 吗？`" @ok="popDeleteFunc(record)">
                    <a-button type="text" style="padding:5px" @click.stop :disabled="record.completed==3||record.completed==0"> 删除</a-button>
                </a-popconfirm>
            </template>
        </a-table>
    </a-modal>
</template>
<script setup>
import addRule from "./com/add-rule.vue";
import { Message } from "@arco-design/web-vue";
import { toFixed } from "../../../utils";
import { computed, onUnmounted, ref } from "vue";
import { useRoute } from "vue-router";
import emitter from "@/utils/emitter";
import {
    getRuleList,
    ruleDelete,
    getOverView,
} from "@/api/task/text/ruleSet.js";
import {
    getIntegrationRunning,
    IntegrationRule,
} from "@/api/task/text/autoLabel.js";
const router = useRoute();
const taskId = ref(router.params.id);
const taskName = ref(router.params.name);
const recordObj = ref("");
const showUnGoodData = ref([]);
const visible = ref(false);
const isAdd = ref(false);
const isRunning = ref(false);
const state = ref({
    labelCount: 0,
    conflict: 0,
    accuracy: 0,
    coverage: 0,
});
const accuracy = ref(50);
const coverage = ref(20);
const data = ref([]);
const isRunningFunc = computed(() => {
    return (r, val) => {
        if (r.completed == 0) {
            return "-";
        } else {
            return val;
        }
    };
});
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        width: "10",
        slotName: "rowIndex",
    },
    {
        title: "标注规则",
        dataIndex: "ruleName",
        width: 120,
    },
    {
        title: "标签名称",
        dataIndex: "labelDes",
        width: 80,
        render: ({ record }) => {
            return `${record.labelDes || "-"}`;
        },
    },
    {
        title: "验证集",
        children: [
            {
                title: "准确率",
                dataIndex: "accuracy",
                width: 90,
                slotName: "accuracy",
            },
            {
                title: "覆盖率",
                dataIndex: "coverage",
                width: 80,
                slotName: "coverage",
            },
        ],
    },
    {
        title: "未标注数据集",
        children: [
            {
                title: "覆盖率",
                dataIndex: "unlabeledCoverage",
                width: 80,
                render: ({ record }) => {
                    if (record.completed == 0) return "-";

                    return record.unlabeledCoverage
                        ? `${toFixed(record.unlabeledCoverage)}%`
                        : "-";
                },
            },
            {
                title: "重叠率",
                dataIndex: "overlap",
                width: 80,
                render: ({ record }) => {
                    if (record.completed == 0) return "-";
                    return record.overlap ? `${toFixed(record.overlap)}%` : "-";
                },
            },
            {
                title: "冲突率",
                dataIndex: "conflict",
                width: 80,

                render: ({ record }) => {
                    if (record.completed == 0) return "-";
                    return record.conflict
                        ? `${toFixed(record.conflict)}%`
                        : "-";
                },
            },
        ],
    },
    {
        title: "规则状态",
        dataIndex: "completed",
        width: 70,
        slotName: "completed",
    },
    {
        title: "操作",
        slotName: "operation",
        width: "120",
        fixed: "right",
    },
]);
const modelColumns = computed(() => {
    return columns.value.filter((x) => x.title != "规则状态");
});
const getOverViewData = async () => {
    let res = await getOverView({
        taskId: taskId.value,
    });
    Object.assign(state.value, res.data);
};
const getIsIntegrated = async (val) => {
    let res = await getIntegrationRunning({
        taskId: taskId.value,
    });
    isRunning.value = res.data;
};
const getData = async () => {
    let res = await getRuleList({
        taskId: taskId.value,
    });
    data.value = res.data;
    getOverViewData();
    getIsIntegrated();
};
//开始规则集成
const startIntegration = async () => {
    let completed = data.value.find((val) => !val.completed);
    if (completed) {
        Message.error("有规则尚未构建完成！");
    } else {
        showUnGoodData.value = data.value.filter((val) => {
            return val.accuracy < accuracy.value || val.completed == 2;
        });
        if (showUnGoodData.value.length) {
            visible.value = true;
        } else {
            //直接开始集成
            handleOk();
        }
    }
};
//集成方法
const handleOk = async (done) => {
    try {
        let res = await IntegrationRule({
            taskId: taskId.value,
        });
        Message.success("开始集成！");
        getIsIntegrated();
        done && done();
    } catch (error) {
        done && done(false);
    }
};
//添加规则
const addRuleFunc = () => {
    recordObj.value = "";
    isAdd.value = true;
};
//弹框删除规则
const popDeleteFunc = async (record) => {
    try {
        let res = await ruleDelete({
            taskId: taskId.value,
            ruleId: record.id,
        });
        Message.success("删除成功");
        getData();
        showUnGoodData.value = data.value.filter((val) => {
            return val.accuracy < accuracy.value || val.completed == 2;
        });
    } catch (error) {}
};
//删除规则
const deleteFunc = async (record) => {
    try {
        let res = await ruleDelete({
            taskId: taskId.value,
            ruleId: record.id,
        });
        Message.success("删除成功");
        getData();
    } catch (error) {}
};
//添加规则
const addOk = () => {
    isAdd.value = false;
    getData();
};
//编辑规则
const editRule = (record) => {
    isAdd.value = true;
    recordObj.value = record;
};
const popEditRule = (record) => {
    visible.value = false;
    isAdd.value = true;
    recordObj.value = record;
};
getData();
emitter.on("integrated", (data) => {
    if (data.taskId == taskId.value) {
        getData();
    }
});
emitter.on("rule", (data) => {
    if (data.taskId == taskId.value) {
        getData();
    }
});
emitter.on("text_click", (data) => {
    if (data.taskId == taskId.value && data.state == 6) {
        getData();
    }
});
onUnmounted(() => {
    emitter.off("integrated");
    emitter.off("rule");
    emitter.off("text_click");
});
</script>
<style lang='less' scoped>
.row {
    display: flex;
    flex: 1;
    border: 1px solid var(--color-border-4);
    // background: var(--color-fill-1);
    & + .row {
        border-top: none;
    }
    > div {
        flex: 20%;
        text-align: center;
        line-height: 25px;
        & + div {
            border-left: 1px solid var(--color-border-4);
        }
    }
    .flex2 {
        flex: 40%;
    }
    .flex3 {
        flex: 60%;
    }
}
.red {
    color: rgb(var(--red-6));
}
.green {
    color: rgb(var(--green-6));
}
.yellow {
    color: rgb(var(--orange-6));
}
</style>