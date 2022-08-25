<template>
    <div class="container">
        <Breadcrumb :items="BreadcrumbList" />
        <a-card>
            <a-tabs type="capsule" :active-key="activeKey" :lazy-load="true" @change="e=>activeKey=e">
                <a-tab-pane key="1" title="上传数据集">
                    <dataSet  />
                </a-tab-pane>
                <a-tab-pane key="3" title="自动标注">
                    <autoLabel  />
                </a-tab-pane>
                <a-tab-pane key="4" title="模型训练">
                    <trainSet />
                </a-tab-pane>
            </a-tabs>
        </a-card>
    </div>
</template>
<script setup>
import { ref, provide } from "vue";
import router from "../../router";
import dataSet from "./ner-com/data-set.vue";
import autoLabel from "./ner-com/auto-label.vue";
import trainSet from "./ner-com/train-set.vue";

const taskName = ref(router.currentRoute.value.params.name);
const activeKey = ref("1");
const BreadcrumbList = [
    {
        name: "任务管理",
        func: () => {
            router.push("/task");
        },
    },
    {
        name: taskName.value,
    },
];

</script>
<style lang='less' scoped>
.container {
    padding: 16px 20px;
    padding-bottom: 0;
    background-color: var(--color-fill-2);
    > .arco-card {
        width: 100%;
    }
}
:deep(.arco-tabs-nav-tab) {
    justify-content: flex-start;
}
</style>