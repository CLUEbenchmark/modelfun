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
    </a-modal>
    <a-modal v-model:visible="detailVisible" width='800px' unmount-on-close ok-text='返回' hide-cancel>
        <template #title>
            查看详情
        </template>
        <div style="height:700px">
            <a-table :columns="columns" :data="data" :scroll="{y:'100%'}" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange' :bordered="{wrapper: true, cell: true}">
                <template #index="{rowIndex}">
                    {{rowIndex+1}}
                </template>
                <template #actual="{ record }">
                    <span v-html="sentenceComputed(record.actual,record.sentence)"></span>
                </template>
                <template #predict="{ record }">
                    <span v-html="sentenceComputed(record.predict,record.sentence)"></span>
                </template>
            </a-table>
        </div>

    </a-modal>
</template>
<script setup>
import { getLabelDetail, getLabelDiff } from "@/api/task/ner/train.js";
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
        title: "原始内容",
        dataIndex: "actual",
        slotName: "actual",
    },
    {
        title: "标注结果",
        dataIndex: "predict",
        slotName: "predict",
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
let color = [
    "F53F3F",
    "F7BA1E",
    "D91AD9",
    "F77234",
    "3491FA",
    "00B42A",
    "F77234",
];
const sentenceComputed = computed(() => {
    return (v, sentence) => {
        let arr = sentence.split("");
        let result = [];
        let colorMap = [];
        v.map((val) => {
            if (!colorMap.find((item) => item.label == val.label)) {
                colorMap.push({
                    label: val.label,
                    color: color[colorMap.length],
                });
            }
        });
        v.map((val, i) => {
            let num = val.end_offset - val.start_offset;
            let flag = arr.splice(val.start_offset, num);
            let result = `<span  style='color:#${
                colorMap.find((x) => x.label == val.label).color
            }'>${flag.join("")}<span class="unitSPan">[${
                val.label
            }]</span></span>`;
            for (let index = 0; index < num; index++) {
                if (index == 0) {
                    arr.splice(val.start_offset, 0, result);
                } else {
                    arr.splice(val.start_offset + 1, 0, "");
                }
            }
        });
        return arr.join("");
    };
});
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
defineExpose({
    init,
    cencel,
});
</script>
<style lang='less' scoped>
</style>