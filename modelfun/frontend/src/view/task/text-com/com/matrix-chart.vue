<template>
    <a-row class="grid-demo">
        <a-col :span="12">
            <Chart height="400px" :options="chartOption" @clickFunc="clickFunc" />
        </a-col>
        <a-col :span="12">
            <a-table :columns="columns" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
                <template #index="{rowIndex}">
                    {{rowIndex+1}}
                </template>
            </a-table>
        </a-col>
    </a-row>

</template>

<script setup>
import { useRoute } from "vue-router";
import { ref } from "vue";
import { getConfusionMatrix, getMatrixDtail } from "@/api/task/text/train.js";
import usePage from "@/hooks/usePage.js";
const { pagination, pageChange, pageSizeChange } = usePage(
    () => getDetailData(),
    {
        showJumper: false,
        showPageSize: false,
    }
);
const router = useRoute();
const taskId = ref(router.params.id);
const labels = ref([]);
const data = ref([]);
const columns = ref([
    {
        title: "原始语料",
        dataIndex: "sentence",
        width: "220",
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
const nowData = ref({});
const clickFunc = (params) => {
    console.log(params);
    nowData.value = params;
    pagination.current = 1;
    getDetailData();
};
const chartOption = ref({
    tooltip: {
        position: "top",
        formatter: (params) => {
            return (
                "预测：" +
                labels.value[params.data[1]] +
                "<br>实际：" +
                labels.value[params.data[0]] +
                "<br>数目：" +
                params.data[2]
            );
        },
    },
    grid: {
        height: "80%",
        top: "0%",
        left: "15%",
    },
    xAxis: {
        type: "category",
        data: labels.value,

        axisLabel: {
            interval: 0,
            rotate: -30,
        },
        splitArea: {
            show: true,
        },
    },
    yAxis: {
        type: "category",
        data: labels.value,
        splitArea: {
            show: true,
        },
    },
    visualMap: {
        calculable: true,
        max: 10,
        orient: "horizontal",
        left: "center",
        bottom: "0%",
        inRange: {
            color: [
                "rgba(0,100,255,0)",
                "rgba(0,100,255,0.1)",
                "rgba(0,100,255,0.2)",
                "rgba(0,100,255,0.3)",
                "rgba(0,100,255,0.4)",
                "rgba(0,100,255,0.5)",
                "rgba(0,100,255,0.6)",
                "rgba(0,100,255,0.7)",
                "rgba(0,100,255,0.8)",
                "rgba(0,100,255,0.9)",
                "rgba(0,100,255,1)",
            ],
        },
    },
    series: [
        {
            type: "heatmap",
            data: [],
            label: {
                show: true,
            },
            emphasis: {
                itemStyle: {
                    shadowBlur: 10,
                    shadowColor: "rgba(0, 0, 0, 0.5)",
                },
            },
        },
    ],
});
const getDetailData = async () => {
    let res = await getMatrixDtail({
        recordId: props.state.trainRecordId,
        taskId: taskId.value,
        predict: labels.value[nowData.value.data[1]],
        actual: labels.value[nowData.value.data[0]],
        ...pagination,
    });
    data.value = res.data;
    pagination.total = res.total;
};
const getData = async () => {
    let res = await getConfusionMatrix({
        recordId: props.state.trainRecordId,
        taskId: taskId.value,
    });
    labels.value = res.data.labels;
    chartOption.value.xAxis.data = res.data.labels;
    chartOption.value.yAxis.data = res.data.labels;
    let arr = [];
    let max = 0;
    res.data.matrix.map((yItem, y) => {
        yItem.map((xItem, x) => {
            if (xItem >= max) {
                max = xItem;
            }
            arr.push([y, x, xItem]);
        });
    });
    chartOption.value.series[0].data = arr;
    chartOption.value.visualMap.max = max;
};
getData();
const props = defineProps(["state"]);
</script>
