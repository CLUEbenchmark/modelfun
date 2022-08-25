<template>
    <div class="search-header">
        <div class="search-header-right">
            <a-button type="primary" @click="train" :loading="isRunning">{{isRunning?'正在训练':'模型训练'}}</a-button>
        </div>
    </div>
    <a-table :columns="columns" :data="data" :pagination="false" :bordered="{wrapper: true, cell: true}">
        <template #index="{rowIndex}">
            {{rowIndex+1}}
        </template>
        <template #operation="{ record }">
            <a-button type="text" style="padding:5px" @click="downloadFile(record,1)">下载数据</a-button>
            <a-button type="text" style="padding:5px" @click="downloadFile(record,2)">下载模型</a-button>
            <a-button type="text" style="padding:5px" @click="openApi(record)">数据分析</a-button>
        </template>
        <template #trainStatus="{record}">
            <span :style="{color:(record.trainStatus==2||record.trainStatus==3)?'red':''}">{{['训练中','已完成','服务错误','网络错误'][record.trainStatus]}}</span>
        </template>
    </a-table>
    <a-pagination class="pagination" :total="pagination.total" v-model="pagination.current " :page-size="pagination.pageSize" @page-size-change="pageSizeChange" @change="pageChange" show-page-size showJumper showTotal></a-pagination>

    <dataAnalysis ref="dataAnalysisRef" />
</template>
<script setup>
import { ref, onUnmounted } from "vue";
import usePage from "@/hooks/usePage.js";
import {
    getTrainList,
    startTrain,
    getTrainRunning,
} from "@/api/task/ner/train.js";
import { useRoute } from "vue-router";
import { Message, Notification, Modal } from "@arco-design/web-vue";
import { getFileUrl } from "@/api/oss.js";
import emitter from "@/utils/emitter.js";
import { toFixed } from "../../../utils";
import dataAnalysis from "./com/data-analysis.vue";
const dataAnalysisRef = ref(null);
const data = ref([]);
const { pagination, pageChange, pageSizeChange } = usePage(() => getData());
const router = useRoute();
const taskId = ref(router.params.id);
const taskName = ref(router.params.name);
const type = ref("1");
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        slotName: "index",
        width: "80",
    },
    {
        title: "数据版本",
        dataIndex: "dataVersion",
        render: ({ record, column, rowIndex }) => {
            return record.dataVersion || "-";
        },
    },
    {
        title: "训练数据量",
        dataIndex: "trainCount",
    },
    {
        title: "标签类别",
        dataIndex: "labelTypeCount",
    },
    {
        title: "准确率",
        dataIndex: "accuracy",
        render: ({ record }) => {
            return record.accuracy !== undefined
                ? `${toFixed(record.accuracy)}%`
                : "-";
        },
    },
    {
        title: "精准率",
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
        title: "F1 score",
        dataIndex: "f1Score",
        render: ({ record }) => {
            return record.f1Score !== undefined
                ? `${toFixed(record.f1Score, 2)}`
                : "-";
        },
    },
    {
        title: "创建时间",
        dataIndex: "createDatetime",
    },
    {
        title: "训练状态",
        slotName: "trainStatus",
    },
    {
        title: "操作",
        dataIndex: "label",
        slotName: "operation",
        width: "240",
        fixed: "right",
    },
]);
const getData = async (val) => {
    const res = await getTrainList({
        taskId: taskId.value,
        ...pagination,
    });
    data.value = res.data;
    pagination.total = res.total;
    getIsTrain();
};
const train = async () => {
    handleOk()
};
const downloadFile = async (record, type) => {
    let str = type == 1 ? "trainFileAddress" : "modelFileAddress";
    let res = await getFileUrl({
        filePath: record[str],
    });
    var a = document.createElement("a");
    a.href = res.data;
    a.style.display = "none";
    document.body.appendChild(a);
    a.click();
    a.remove();
};
const isRunning = ref(false);
const getIsTrain = async (val) => {
    let res = await getTrainRunning({
        taskId: taskId.value,
    });
    isRunning.value = res.data;
};
let obj = {
    userid: "1234",
    sentence: "xxxxxx",
};
let arr = JSON.stringify(obj, null, 4).split("\n");
let obj2 = {
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
var result = JSON.stringify(obj2, null, 4); //格式化后的json字符串形式
let arr2 = result.split("\n");
const codeText = ref([arr.join("\n"), arr2.join("\n")]);
const visible = ref(false);
const openApi = (val) => {
    dataAnalysisRef.value.init(val) 
};
const handleOk = async () => {
        let res = await startTrain({
            taskId: taskId.value,
            model: type.value,
        });
        Message.success({
            content: "模型训练开始",
        });
        isRunning.value = true;
};
getData();
emitter.on("train", (data) => {
    if (data.taskId == taskId.value) {
        getData(1);
    }
});
emitter.on("text_click", (data) => {
    if (data.taskId == taskId.value && data.state == 6) {
        getData();
    }
});
onUnmounted(() => {
    emitter.off("train");
    emitter.off("text_click");
});
</script>
<style lang='less' scoped>
.pagination {
    float: right;
    margin-top: 16px;
}
</style>