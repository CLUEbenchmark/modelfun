<template>

    <a-modal v-model:visible="visible" title-align='start' @ok="apiVisible=false" ok-text='返回' width="1000px" unmount-on-close hide-cancel>
        <template #title>数据分析</template>
        <a-alert type="warning" :show-icon="false">
            <div>
                评估结论：
            </div>
            <div>
                1. 您当前评估的数据集版本为 <span>{{state.dataVersion}}</span>
            </div>
            <div>
                2. 精准率：{{toFixed(state.accuracy)}}%，召回率：{{toFixed(state.recall)}}% ， F1 score：{{toFixed(state.f1Score)}}
            </div>
        </a-alert>
        <h2>1. 错误样本</h2>
        <a-table style="margin-top:16px" :columns="baseColumns" :data="baseData" :pagination="false" :bordered="{wrapper: true, cell: true}">
            <template #index="{rowIndex}">
                {{rowIndex+1}}
            </template>
            <template #operation="{ record }">
                <a-button type="text" style="padding:5px" @click="showDetail(record)">查看详情</a-button>
            </template>
            <template #trainStatus="{record}">
                <span :style="{color:record.trainStatus==2?'red':''}">{{['训练中','已完成','服务错误','网络错误'][record.trainStatus]}}</span>
            </template>
        </a-table>
        <div class="title">
        <h2>2. 混淆矩阵</h2>
        <a-link @click="downloadData">下载完整评估结果</a-link>
        </div>
        <matrixChart :state="state" />
    </a-modal>
    <a-modal v-model:visible="detailVisible" width='800px' unmount-on-close ok-text='返回' hide-cancel>
        <template #title>
            查看详情
        </template>
        <div style="max-height:700px">
            <a-table :columns="columns" :data="data" :scroll="{y:'100%'}" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange' :bordered="{wrapper: true, cell: true}">
                <template #index="{rowIndex}">
                    {{rowIndex+1}}
                </template>
            </a-table>
        </div>

    </a-modal>
</template>
<script setup>
import matrixChart from './matrix-chart.vue'
import { getLabelDetail, getLabelDiff,getModelDownload } from "@/api/task/text/train.js";
import { ref, computed } from "vue";
import { useRoute } from "vue-router";
import { toFixed } from "@/utils";
import usePage from "@/hooks/usePage.js";
const { pagination, pageChange, pageSizeChange } = usePage(() =>
    getDetailData()
);
const nowData = ref({});
const detailVisible = ref(false);
const router = useRoute();
const taskId = ref(router.params.id);
const baseData = ref([]);
const data = ref([]);
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        slotName: "index",
        width: "80",
    },
    {
        title: "原始语料",
        dataIndex: "sentence",
    },
    {
        title: "实际类别",
        dataIndex: "actual",
    },
    {
        title: "预测类别",
        dataIndex: "predict",
    },
]);
const baseColumns = ref([
    {
        title: "类别",
        dataIndex: "labelDes",
    },
    {
        title: "精确率",
        dataIndex: "trainPrecision",
        render: ({ record }) => {
            return record.trainPrecision !== undefined
                ? `${toFixed(record.trainPrecision)}%`
                : "-";
        },
    },
    {
        title: "召回率",
        dataIndex: "recall",
        render: ({ record }) => {
            return record.recall !== undefined
                ? `${toFixed(record.recall)}%`
                : "-";
        },
    },
    {
        title: "样本数",
        dataIndex: "samples",
    },
    {
        title: "预测错误数",
        dataIndex: "errorCount",
    },
    {
        title: "操作",
        dataIndex: "label",
        slotName: "operation",
        width: "140",
        fixed: "right",
    },
]);
const state = ref({});
const visible = ref(false);
const cencel = () => {
    visible.value = false;
};
const init = (val) => {
    state.value = val;
    visible.value = true;
    getData();
};
const showDetail = (val) => {
    data.value = [];
    detailVisible.value = true;
    nowData.value = val;
    pagination.current = 1;
    getDetailData();
};
const getDetailData = async () => {
    let res = await getLabelDiff({
        taskId: taskId.value,
        recordId: nowData.value.id,
        trainRecordId: nowData.value.trainRecordId,
        ...pagination,
    });
    console.log(res);
    pagination.total = res.total;
    data.value = res.data;
};
const getData = async () => {
    let res = await getLabelDetail({
        taskId: taskId.value,
        recordId: state.value.trainRecordId,
    });
    baseData.value = res.data;
};
const downloadData = async () => {
    let res = await getModelDownload({
        taskId: taskId.value,
        recordId: state.value.trainRecordId
    });
    console.log(res)
    window.open(res.data);
};
defineExpose({
    init,
    cencel,
});
</script>
<style lang='less' scoped>
.title{
    display: flex;
    align-items: center;
    h2{
        margin-right:30px
    }
}
</style>