<template>
    <a-drawer :visible="showLabel" unmountOnClose :mask="false" width="100%" ok-text="关闭" popup-container="#parentNode" hide-cancel @cancel="showLabel=false" @ok="showLabel=false" :drawer-style="noMaskClass">
        <template #title>
            标签集
        </template>
        <div>
            <div class="search-header">
            <a-input v-model="searchText" style="width:300px;margin-right:20px" @press-enter='search' placeholder="请输入关键词按“enter”进行查找"></a-input>
            <a-button type="primary" @click="reset">重置</a-button>
        </div>
            <a-table :columns="columns" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
                <template #rowIndex="{ rowIndex }">
                    {{rowIndex+1}}
                </template>
            </a-table>
        </div>
    </a-drawer>
</template>
<script setup>
import { ref, reactive } from "vue";
import usePage from "@/hooks/usePage.js";
import { useRoute } from "vue-router";
import {
    getDatasetDetail
} from "@/api/task/text/dataSet.js";
const router = useRoute();
const taskId = ref(router.params.id);
const { pagination, pageChange, pageSizeChange } = usePage(() => getData());
const searchText = ref("");
const columns = [
    {
        title: "标签 id",
        dataIndex: "label",
    },
    {
        title: "标签名称",
        dataIndex: "labelDes",
    },
];
const showLabel = ref(false);
const noMaskClass = reactive({
    "box-shadow": "2px 2px 10px #909090",
});
const data = ref([]);
const init = () => {
    showLabel.value = true;
    pagination.current = 1;
    getData();
};
const search = async () => {
    pagination.current = 1;
    getData();
};
const getData = async () => {
    let res = await getDatasetDetail({
        taskId: taskId.value,
        ...pagination,
        sentence: searchText.value,
        dataType: 3,
    });
    data.value = res.data;
    pagination.total = res.total;
};
const reset = () => {
    searchText.value = "";
    pagination.current = 1;
    getData();
};
defineExpose({
    init,
});
</script>
<style lang='less' scoped>
</style>