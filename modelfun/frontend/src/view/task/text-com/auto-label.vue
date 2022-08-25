<template>
    <a-alert type="warning" :show-icon="false">
        <a-row align="center">
            <a-col flex="none">标注概览（总）</a-col>
            <a-col :flex="1">
                <div class="row">
                    <div class="flex2">
                        训练集
                    </div>
                    <div class="flex3">
                        测试集
                    </div>
                    <div>
                        未标注数据集
                    </div>
                    <div>
                        标注耗时
                    </div>
                </div>
                <div class="row">
                    <div>
                        标签类别：{{state.trainLabelCount||'0'}} 类
                    </div>
                    <div>
                        语料标注量：{{state.trainSentenceCount||'0'}} 条
                    </div>
                    <div>
                        准确率：{{state.testAccuracy||'0.00'}}%
                    </div>
                    <div>
                        召回率：{{state.testRecall||'0.00'}}%
                    </div>
                    <div>
                        F1 score：{{state.testF1Score||'0'}}
                    </div>
                    <div>
                        覆盖率：{{state.unlabelCoverage||'0.00'}}%
                    </div>
                    <div>
                        耗时：{{toFixed(state.timeCost/60)}}min
                    </div>
                </div>
            </a-col>
        </a-row>
    </a-alert>
    <a-tabs :active-key="active" @change="activeChange"  style="margin-top:16px">
        <template #extra>
            <a-space>
                <span v-if="state.lastUpdateTime">最近更新时间：{{state.lastUpdateTime}}</span>
                <a-button type="primary" @click="train" :loading="isRunning">{{isRunning?'正在标注':'开始标注'}}</a-button>
                <a-button type="primary" @click="batchDel" :disabled="isRunning">批量删除</a-button>
            </a-space>
        </template>
        <a-tab-pane :key="2" title="待审核数据">
        </a-tab-pane>
        <a-tab-pane :key="1" title="高置信数据">
        </a-tab-pane>
    </a-tabs>
    <div class="header" style="margin:16px 0">
        <a-space wrap>
            <span>问法语料：</span>
            <a-input placeholder="请输入关键词进行搜索" style="width:300px" v-model="sentence"></a-input>
            <span>标签：</span>
            <a-select v-model="label" @search="handleSearch" allow-search :loading="loading" :filter-option="false" style="width:170px" placeholder="请输入想要选择的标签">
                <a-option v-for="(item,index) in labelOptionsList" :key="index" :value="item.label">{{item.labelDes}}</a-option>
            </a-select>
            <a-button type="primary" @click="getData">查找</a-button>
            <a-button type="primary" @click="reset">重置</a-button>
        </a-space>
        <div class="header-right">
            
        </div>
    </div>
    <a-table :columns="columns" row-key="id" :row-selection="rowSelection" @selection-change="e=>selectedKeys=e" v-model:selectedKeys="selectedKeys" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
        <template #rowIndex="{ rowIndex }">
            {{rowIndex+1}}
        </template>
        <template #action="{record}">
            <a-button type="text" @click="handleEdit(record)" style="padding:5px">编辑</a-button>
            <a-popconfirm :content="`确认删除该条标注结果吗？`" @ok="handleDel([record.id])">
                <a-button type="text" status="danger" style="padding:5px">删除</a-button>
            </a-popconfirm>

        </template>
    </a-table>
    <a-modal v-model:visible="visible" title-align="start" modalClass="warning" :on-before-ok="handleBeforeOk" unmountOnClose>
        <template #title>
            <icon-exclamation-circle-fill class="warning" />
            批量删除
        </template>
        <div>
            确认要删除选中的{{selectedKeys.length}}条标注结果吗？
        </div>
    </a-modal>
    <a-modal v-model:visible="editVisible" :on-before-ok="editHandleBeforeOk" unmountOnClose ok-text="保存并同步">
        <template #title>
            修改
        </template>
        <a-form :model='form' auto-label-width>
            <a-form-item label="语料问法：" disabled>
                <a-textarea v-model="form.sentence" placeholder="请输入标签说明示例" auto-size></a-textarea>
            </a-form-item>
            <a-form-item label="标签：">
                <a-select placeholder="请选择标签" v-model="form.label">
                    <a-option v-for="(item,index) in labelSelectOptions" :key="index" :value="item.mapKey+''">{{item.mapValue}}</a-option>
                </a-select>
            </a-form-item>

        </a-form>
    </a-modal>
</template>
<script setup>
import { ref, onUnmounted, reactive } from "vue";
import usePage from "@/hooks/usePage.js";
import { useRoute } from "vue-router";
import {
    getIntegrationList,
    getLabelRunning,
    getIntegrationOverview,
    startIntegration,
    delAutoLabel,
    editAutoLabel,
} from "@/api/task/text/autoLabel.js";
import { getLabelOptions } from "@/api/task/text/ruleSet.js";
import { get, useDebounceFn } from "@vueuse/core";
import { Message, Notification } from "@arco-design/web-vue";
import emitter from "@/utils/emitter.js";
import useLoading from "@/hooks/loading";
import { toFixed } from "../../../utils";
import { Modal } from "@arco-design/web-vue";
const { loading, setLoading } = useLoading();
const router = useRoute();
const taskId = ref(router.params.id);
const taskName = ref(router.params.name);
const state = ref({});
const data = ref([]);
const form = ref({});
const { pagination } = usePage(() => getData());
const visible = ref(false);
const editVisible = ref(false);
const active = ref(1)
const pageChange = (page) => {
    console.log(selectedKeys.value);
    if (selectedKeys.value.length) {
        Modal.warning({
            hideCancel: false,
            cancelText: "取消",
            title: "提示",
            closable: true,
            content: "该操作会清空已选择的数据！",
            onOk: () => {
                selectedKeys.value = [];
                pagination.current = page;
                getData(1);
            },
        });
    } else {
        pagination.current = page;
        getData(1);
    }
};
const pageSizeChange = (pageSize) => {
    pagination.pageSize = pageSize;
    getData(1);
};
const sentence = ref("");
const label = ref("");
const selectedKeys = ref([]);
const rowSelection = ref({
    type: "checkbox",
    showCheckedAll: true,
    fixed: true,
});
const activeChange =(e)=>{
    console.log(e)
    active.value = e
    getData()
}
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        width: "80",
        slotName: "rowIndex",
    },
    {
        title: "语料问法",
        dataIndex: "sentence",
    },
    {
        width: "140",
        title: "标签名称",
        dataIndex: "labelDes",
    },
    {
        width: "140",
        title: "操作",
        slotName: "action",
    },
]);
const getData = async (val) => {
    const res = await getIntegrationList({
        taskId: taskId.value,
        sentence: sentence.value,
        labelId: label.value,
        dataType:active.value,
        ...pagination,
    });
    data.value = res.data;
    pagination.total = res.total;
    !val && getIsTrain();
    !val && getInfo();
};
const train = async () => {
    let res = await startIntegration({
        taskId: taskId.value,
    });
    Message.success("开始自动标注！");
    getIsTrain();
    isRunning.value = true;
};
const isRunning = ref(false);
const reset = () => {
    sentence.value = "";
    label.value = "";
    handleSearch();
    getData();
};
const getIsTrain = async (val) => {
    let res = await getLabelRunning({
        taskId: taskId.value,
    });
    isRunning.value = res.data;
};
const getInfo = async () => {
    let res = await getIntegrationOverview({
        taskId: taskId.value,
    });
    Object.assign(state.value, res.data);
};
const getLabelOptionsList = (val) => {
    return getLabelOptions({
        taskId: taskId.value,
        labelDesc: val,
        pageNum: 1,
        pageSize: 200,
    });
};
const labelOptionsList = ref([]);
const handleSearch = useDebounceFn(async (value) => {
    setLoading(true);
    try {
        let res = await getLabelOptionsList(value);
        labelOptionsList.value = res.data.map((x) => {
            return {
                label: x.mapKey * 1,
                labelDes: x.mapValue,
            };
        });
    } catch (error) {
    } finally {
        setLoading(false);
    }
}, 500);
const batchDel = () => {
    if (selectedKeys.value.length == 0) {
        Message.error("未选择任何数据");
        return;
    }
    visible.value = true;
};
const handleBeforeOk = (done) => {
    handleDel(selectedKeys.value);
    done();
};
const handleDel = async (val) => {
    let res = await delAutoLabel({
        taskId: taskId.value,
        recordIds: val,
    });
    Message.success("删除成功！");
    getData();
};
const labelSelectOptions = ref([]);
const handleEdit = (val) => {
    form.value = val;
    editVisible.value = true;
};
const editHandleBeforeOk = async (done) => {
    try {
        let res = await editAutoLabel({
            taskId: taskId.value,
            dataType:active.value,
            recordId: form.value.id,
            labelId: form.value.label,
        });
        Message.success("修改成功！");
        getData();
        done();
    } catch (error) {
        done(false);
    }
};
const getAll = async () => {
    let res = await getLabelOptions({
        taskId: taskId.value,
        pageNum: 1,
        pageSize: 200,
    });
    labelSelectOptions.value = res.data;
};
getAll();
reset();
emitter.on("auto_label", (data) => {
    if (data.taskId == taskId.value) {
        getData();
    }
});
emitter.on("text_click", (data) => {
    if (data.taskId == taskId.value && data.state == 6) {
        getData();
    }
});
onUnmounted(() => {
    emitter.off("auto_label");
    emitter.off("text_click");
});
</script>
<style lang='less' scoped>
.header {
    display: flex;
    .header-right {
        margin-left: auto;
    }
}
.red {
    color: red;
}
.big {
    font-size: 20px;
}
.row {
    display: flex;
    flex: 1;
    border: 1px solid var(--color-border-4);
    // background: var(--color-fill-1);
    & + .row {
        border-top: none;
    }
    > div {
        width: 16.666%;
        text-align: center;
        line-height: 25px;
        & + div {
            border-left: 1px solid var(--color-border-4);
        }
    }
    .flex2 {
        width: 33.333%;
    }
    .flex3 {
        width: 50%;
    }
}
.warning {
    color: rgb(var(--warning-6));
    margin-right: 5px;
}
</style>
<style lang="less">
.warning {
    .arco-modal-header {
        border-bottom: none !important;
    }
    .arco-modal-footer {
        border-top: none !important;
    }
}
</style>