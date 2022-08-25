<template>
    <a-card hoverable class="project-card">
        <div class="title">
            <span>{{taskData.name}}</span>
            <a-button type="text" style="margin-left:auto;padding:5px" @click.stop="editFunc(taskData)"> 编辑</a-button>
            <a-popconfirm :content="`确认删除 ${taskData.name} 吗？`" @ok="deleteFunc">
                <a-button type="text" style="padding:5px" @click.stop> 删除</a-button>
            </a-popconfirm>
        </div>
        <div class="desc">
            {{taskData.description}}
        </div>
        <div class="item">
            <label>任务类型：</label><span>{{taskData.taskType==1?'文本类型':'ner'}}</span>
        </div>
        <div class="item">
            <label>未标注数据量：</label><span>{{taskData.unlabeledCount}}条</span>
        </div>
        <div class="item">
            <label>已标注数据量：</label><span>{{taskData.labeledCount}}条</span>
        </div>
        <div class="bottom">
            <span>更新于{{taskData.updateDatetime.substr(0,10)}}</span>
        </div>
    </a-card>
</template>
<script setup>
import { inject } from "vue";
import { deleteTask } from "@/api/task";
import { Message } from "@arco-design/web-vue";
const props = defineProps({
    taskData: {
        type: Object,
        default: () => {},
    },
});
const deleteFunc = async () => {
    const res = await deleteTask({
        id: props.taskData.id,
    });
    Message.success("删除成功");
    updateList();
};
const updateList = inject("updateList");
const editFunc = inject("editFunc");
</script>
<style lang='less' scoped>
.project-card {
    width: 300px;
    height: 250px;
    flex-shrink: 0;
    margin: 0 20px 20px 0;
    display: flex;
    flex-direction: column;
    cursor: pointer;
    position: relative;
    :deep(.arco-card-body) {
        display: flex;
        height: 100%;
        box-sizing: border-box;
        flex-direction: column;
        padding-bottom: 31px;
    }
    .item {
        line-height: 1.5;
    }
    .title {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        span {
            font-size: 18px;
            font-weight: 500;
        }
    }
    .desc {
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        color: var(--color-text-2);
        line-height: 1.5;
        flex: 1;
    }
    .bottom {
        position: absolute;
        bottom: 10px;
        right: 10px;
        font-size: 12px;
    }
}
</style>