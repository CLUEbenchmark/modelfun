<template>
    <div class="container">
        <Breadcrumb :items="BreadcrumbList" />
        <a-card>
            <a-tabs type="capsule" :active-key="activeKey" :lazy-load="true" @change="e=>activeKey=e">
                <a-tab-pane key="1" title="上传数据集">
                    <dataSet />
                </a-tab-pane>
                <a-tab-pane key="2" title="构建规则" style="overflow:visible">
                    <ruleSet />
                </a-tab-pane>
                <a-tab-pane key="3" title="自动标注">
                    <autoLabel />
                </a-tab-pane>
                <a-tab-pane key="4" title="模型训练">
                    <trainSet />
                </a-tab-pane>
            </a-tabs>
        </a-card>
    </div>
    <a-modal v-model:visible="visible" :closable="false" :footer="false" width="1000px" :esc-to-close="false" :mask-closable="false">
        <div class="oncePop">
            <a-steps style="width:100%">
                <a-step :description="statusText(item)" :status="status(item)" v-for="(item,index) in list" :key="index">
                    {{item.text}}
                    <template #icon>
                        <icon-check-circle-fill v-if="item.index<nowIndex" />
                        <icon-loading v-if="item.index==nowIndex" />
                    </template>
                </a-step>
            </a-steps>
        </div>
    </a-modal>
</template>
<script setup>
import { ref, provide, computed, onUnmounted } from "vue";
import router from "../../router";
import dataSet from "./text-com/data-set.vue";
import ruleSet from "./text-com/rule-set.vue";
import trainSet from "./text-com/train-set.vue";
import autoLabel from "./text-com/auto-label.vue";
import { existTextOpinionTask } from "@/api/task/text/dataSet.js";
import { useRoute } from "vue-router";
import {  Notification } from "@arco-design/web-vue";

import emitter from "@/utils/emitter.js";

const taskName = ref(router.currentRoute.value.params.name);
const active = ref("data");
const activeKey = ref("1");
const visible = ref(false);
const nowIndex = ref(2);
const route = useRoute();
const taskId = ref(route.params.id);
const list = [
    {
        text: "开始标注",
        index: 1,
    },
    {
        text: "构建规则",
        index: 2,
    },
    {
        text: "规则集成",
        index: 3,
    },
    {
        text: "自动标注",
        index: 4,
    },
    {
        text: "模型训练",
        index: 5,
    },
];
const status = computed(() => {
    return (obj) => {
        return obj.index > nowIndex.value
            ? "wait"
            : obj.index == nowIndex.value
            ? "process"
            : "finish";
    };
});
const statusText = computed(() => {
    return (obj) => {
        return obj.index > nowIndex.value
            ? "等待中"
            : obj.index == nowIndex.value
            ? `正在${obj.text}`
            : "已完成";
    };
});
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
const checkOnceTask = async () => {
    let res = await existTextOpinionTask({
        taskId: taskId.value,
    });
    console.log(res);
    if (res.data.exist) {
        visible.value = true;
        nowIndex.value = res.data.state;
    } else {
        visible.value = false;
    }
};
checkOnceTask();
provide("getActive", () => activeKey.value);
provide("onecClick", (val) => {
    visible.value = true;
});
provide("changeActive", (val) => {
    setTimeout(() => {
        activeKey.value = val + "";
    }, 2000);
});

emitter.on("text_click", (data) => {
    if (taskId.value != data.taskId) return;
    if (data.success) {
        nowIndex.value = data.state;
        visible.value = true;
        if (data.state == 6) {
            setTimeout(() => {
                visible.value = false;
                Notification.success({
                    content: '一键标注成功',
                    closable: true,
                    duration: 5000,
                });
            }, 1000);
        }
    } else {
        visible.value = false;
    }
});
onUnmounted(() => {
    emitter.off("text_click");
});
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
.oncePop {
    height: 300px;
    width: 100%;
    display: flex;
    align-items: center;
}
</style>