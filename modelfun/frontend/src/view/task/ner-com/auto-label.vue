<template>
    <a-alert type="warning" :show-icon="false">
        <a-row align="center">
            <a-col flex="none">标注概览（总）</a-col>
            <a-col :flex="1">
                <div class="row">
                    <div class="flex2">
                        标注数据量
                    </div>
                    <div class="flex3">
                        测试集
                    </div>
                    <div>
                        未标注数据集
                    </div>
                    <div>
                        标注耗时
                    </div>
                </div>
                <div class="row">
                    <div>
                        标签类别：{{state.trainLabelCount||'0'}} 类
                    </div>
                    <div>
                        语料标注量：{{state.trainSentenceCount||'0'}} 条
                    </div>
                    <div>
                        精准率：{{state.testAccuracy||'0.00'}}%
                    </div>
                    <div>
                        召回率：{{state.testRecall||'0.00'}}%
                    </div>
                    <div>
                        F1 score：{{state.testF1Score||'0'}}
                    </div>
                    <div>
                        覆盖率：{{state.unlabelCoverage||'0.00'}}%
                    </div>
                    <div>
                        耗时：{{toFixed(state.timeCost/60)}}min
                    </div>
                </div>
            </a-col>
        </a-row>
    </a-alert>
    <a-tabs :active-key="active" @change="e=>active=e" lazy-load style="margin-top:16px">
        <template #extra>
            <a-space>
                <span v-if="state.lastUpdateTime">最近更新时间：{{state.lastUpdateTime}}</span>
                <a-button type="primary" @click="train" :loading="isRunning">{{isRunning?'正在标注':'开始标注'}}</a-button>
            </a-space>
        </template>
        <a-tab-pane :key="1" title="待审核数据">
            <autoTable :dataType="2" />
        </a-tab-pane>
        <a-tab-pane :key="2" title="高置信数据">
            <autoTable :dataType="1" />
        </a-tab-pane>
    </a-tabs>
</template>
<script setup>
import { ref, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import {
    getLabelRunning,
    getIntegrationOverview,
    startIntegration,
} from "@/api/task/ner/autoLabel.js";
import { Message } from "@arco-design/web-vue";
import emitter from "@/utils/emitter.js";
import { toFixed } from "../../../utils";
import labelPop from "./com/label-pop.vue";
import autoTable from "./com/auto-table.vue";
const router = useRoute();
const taskId = ref(router.params.id);
const state = ref({});
const active = ref(1);
let color = [
    "F53F3F",
    "F7BA1E",
    "D91AD9",
    "F77234",
    "3491FA",
    "00B42A",
    "F77234",
];
const train = async () => {
    let res = await startIntegration({
        taskId: taskId.value,
    });
    Message.success("开始自动标注！");
    isRunning.value = true;
};
const isRunning = ref(false);

const getIsTrain = async (val) => {
    let res = await getLabelRunning({
        taskId: taskId.value,
    });
    isRunning.value = res.data;
};
const getInfo = async () => {
    let res = await getIntegrationOverview({
        taskId: taskId.value,
    });
    Object.assign(state.value, res.data);
    getIsTrain()
};
getInfo()
emitter.on("auto_label", (data) => {
    if (data.taskId == taskId.value) {
        getInfo();
    }
});
emitter.on("click", (data) => {
    if (data.taskId == taskId.value) {
        getInfo();
    }
});
onUnmounted(() => {
    emitter.off("auto_label");
    emitter.off("click");
});
</script>
<style lang='less' scoped>
.red {
    color: red;
}
.big {
    font-size: 20px;
}
.row {
    display: flex;
    flex: 1;
    border: 1px solid var(--color-border-4);
    // background: var(--color-fill-1);
    & + .row {
        border-top: none;
    }
    > div {
        width: 16.666%;
        text-align: center;
        line-height: 25px;
        & + div {
            border-left: 1px solid var(--color-border-4);
        }
    }
    .flex2 {
        width: 33.333%;
    }
    .flex3 {
        width: 50%;
    }
}
</style>
<style>
.unitSPan {
    font-size: 12px;
    vertical-align: super;
}
</style>