<template>
    <div class="container">
        <a-spin :loading="loading" style="width:100%">
            <a-card>
                <div class="search-header">
                    <a-input style="width:300px" v-model="taskName" @press-enter='getData' placeholder="输入关键词进行查找" allow-clear @clear="getData"></a-input>
                    <div class="search-header-right">
                        <a-button type="primary" v-cantShow @click="newProjectFunc">新建任务</a-button>
                    </div>
                </div>
                <div class="project-box">
                    <p-card v-for="(item,index) in dataList" :key='index' @click="taskFunc(item)" :taskData="item" />
                    <a-empty style="margin:200px 0" v-show="dataList.length==0">
                        暂无任务
                    </a-empty>
                </div>
            </a-card>
        </a-spin>
        <newProject ref="newProjectRef" />
    </div>
</template>
<script setup>
import { ref, provide } from "vue";
import pCard from "./com/pCard.vue";
import newProject from "./com/newProject.vue";
import router from "../../router";
import { getTaskList } from "@/api/task";
import { store } from "../../store";
import useLoading from "@/hooks/loading";
const newProjectRef = ref(null);
const dataList = ref([]);
const newProjectFunc = () => {
    newProjectRef.value.init();
};
const taskName = ref("");
const { loading, toggle } = useLoading();
const userInfo = store.getters.userInfo;
//获取任务列表
const getData = async () => {
    toggle();
    try {
        const res = await getTaskList({
            userId: userInfo.id,
            name: taskName.value,
        });
        dataList.value = res.data;
    } finally {
        toggle();
    }
};
//跳转到任务详情
const taskFunc = (item) => {
    router.push({
        name: item.taskType == 1 ? "Text" : "Ner",
        params: {
            id: item.id,
            name: item.name,
        },
    });
};
provide("updateList", () => {
    getData();
});
provide("editFunc", (val) => {
    newProjectRef.value.init(val);
});
getData();
</script>
<style lang='less' scoped>
.container {
    padding: 16px 20px;
    padding-bottom: 0;
    background-color: var(--color-fill-2);
    > .arco-card {
        width: 100%;
    }
    .project-box {
        display: flex;
        flex-wrap: wrap;
    }
}
</style>