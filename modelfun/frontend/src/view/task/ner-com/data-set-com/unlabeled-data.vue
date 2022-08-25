<template>
    <a-space wrap>
        <span>问法语料：</span>
        <a-input placeholder="请输入关键词按 ENTER 键进行搜索" v-model="sentence" @press-enter='getData' style="width:300px"></a-input>
        <a-button type="primary" @click="reset">重置</a-button>
    </a-space>
    <a-table style="margin-top:16px" :columns="columns" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
        <template #rowIndex="{ rowIndex }">
            {{rowIndex+1}}
        </template>
    </a-table>
</template>
<script setup>
import { ref, onUnmounted } from "vue";
import { getDatasetDetail } from "@/api/task/text/dataSet.js";
import { useRoute } from "vue-router";
import emitter from "@/utils/emitter.js";
import usePage from "@/hooks/usePage.js";
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        slotName: "rowIndex",
        width: "80",
    },
    {
        title: "语料问法",
        dataIndex: "sentence",
    },
]);
const data = ref([]);
const { pagination, pageChange, pageSizeChange } = usePage(() => getData());
const router = useRoute();
const taskId = ref(router.params.id);
const sentence = ref("");
const reset = () => {
    sentence.value = "";
    getData();
};
const getData = async () => {
    let res = await getDatasetDetail({
        taskId: taskId.value,
        ...pagination,
        dataType: 2,
        sentence: sentence.value,
    });
    data.value = res.data;
    pagination.total = res.total;
};
reset();
emitter.on("dataset_parse", (data) => {
    if (data.taskId == taskId.value) {
        reset();
    }
});
onUnmounted(() => {
    emitter.off("dataset_parse");
});
</script>
<style lang='less' scoped>
</style>