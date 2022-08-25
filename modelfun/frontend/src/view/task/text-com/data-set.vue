<template>
    <a-tabs v-model="active" @change="change">
        <template #extra>
            <a-space>
                <span v-if="active==2">{{uploadDateTime}}未标注量：{{state.unlabelCount}}条</span>
                <span v-else-if="active==8">{{uploadDateTime}}训练集数量：{{state.trainDataCount}}条，训练集类别：{{state.labelCount}}类</span>
                <span v-else-if="active==4">{{uploadDateTime}}验证集数量：{{state.testDataCount}}条，验证集类别：{{state.testDataTypeCount}}类</span>
                <span v-else>{{uploadDateTime}}标签类别：{{state.labelCount}}类</span>
                <a-button type="primary" v-cantShow @click="uploadData" :loading="running">{{running?'正在解析':'上传数据集'}}</a-button>
                <a-button type="primary" @click="onceFunc" :disabled="onceRunning">一键标注</a-button>
            </a-space>
        </template>
        <a-tab-pane :key="2" title="未标注数据集">
            <a-space wrap>
                <span>问法语料：</span>
                <a-input placeholder="请输入关键词按 ENTER 键进行搜索" v-model="sentence" @press-enter='getData' style="width:300px"></a-input>
                <a-button type="primary" @click="reset">重置</a-button>
            </a-space>
        </a-tab-pane>
        <a-tab-pane :key="8" title="训练集">
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
        </a-tab-pane>
        <a-tab-pane :key="4" title="验证集">
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
        </a-tab-pane>
        <a-tab-pane :key="3" title="标签集">
            <a-space wrap>
                <span>标签：</span>
                <a-select v-model="label" @search="handleSearch" allow-search :loading="loading" :filter-option="false" style="width:170px" placeholder="请输入想要选择的标签">
                    <a-option v-for="(item,index) in labelOptionsList" :key="index" :value="item.label">{{item.labelDes}}</a-option>
                </a-select>
                <span>说明：</span>
                <a-input v-model="description" placeholder="请输入标签说明或者示例说明关键词进行搜索" style="width:320px"></a-input>
                <a-button type="primary" @click="getData">查找</a-button>
                <a-button type="primary" @click="reset">重置</a-button>
            </a-space>
        </a-tab-pane>
    </a-tabs>
    <a-table style="margin-top:16px" :columns="baseColumns" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
        <template #rowIndex="{ rowIndex }">
            {{rowIndex+1}}
        </template>
        <template #operation="{record}">
            <a-button type="text" @click="edit(record)">编辑</a-button>
        </template>
    </a-table>
    <addDataPop ref="addDataRef" @addOk="addOk" />
    <a-modal v-model:visible="editVisible" title-align='start' @before-ok="editHandleOk" @cancel="handleCancel" ok-text='保存'>
        <template #title>标签编辑</template>
        <a-form :model='editform' ref="editFormRef" auto-label-width>
            <template v-if="active == 8">
                <a-form-item label="语料问法：" disabled>
                    <a-textarea v-model="editform.sentence" placeholder="请输入标签说明示例" auto-size></a-textarea>
                </a-form-item>
                <a-form-item label="标签名称：" field="label" :rules="[{required:true,message:'请选择标签'}]" :validate-trigger="['blur']">
                    <a-select placeholder="请选择标签" v-model="editform.label">
                        <a-option v-for="(item,index) in labelSelectOptions" :key="index" :value="item.mapKey+''">{{item.mapValue}}</a-option>
                    </a-select>
                </a-form-item>
            </template>
            <template v-else>
                <a-form-item field="labelDes" label="标签名称：" :rules="[{required:true,message:'请上传文件'}]" :validate-trigger="['blur']">
                    <a-input v-model="editform.labelDes" disabled></a-input>
                </a-form-item>
                <a-form-item field="label" label="标签id：" :rules="[{required:true,message:'请上传文件'}]" :validate-trigger="['blur']">
                    <a-input v-model="editform.label" disabled></a-input>
                </a-form-item>
                <a-form-item label="标签说明：">
                    <a-input v-model="editform.description"></a-input>
                </a-form-item>
                <a-form-item label="示例说明：">
                    <a-input v-model="editform.example"></a-input>
                </a-form-item>
            </template>
        </a-form>
    </a-modal>
</template>
<script setup>
import addDataPop from "./com/add-data.vue";
import { ref, computed, reactive, onUnmounted, inject } from "vue";
import { useDebounceFn } from "@vueuse/core";
import usePage from "@/hooks/usePage.js";
import {
    getDatasetDetail,
    getSummaryData,
    updateTagSet,
    updateDatasetLabel,
} from "@/api/task/text/dataSet.js";
import { textOneClick } from "@/api/task.js";
import { getLabelOptions } from "@/api/task/text/ruleSet.js";
import { useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import useLoading from "@/hooks/loading";
import JSZip from "jszip";
import emitter from "@/utils/emitter.js";
const { loading, setLoading } = useLoading();

const data = ref([]);
const { pagination, pageChange, pageSizeChange } = usePage(() => getData());
const active = ref(2);
const Pvisible = ref(false);
const editform = reactive({
    label: "",
    labelDes: "",
    example: "",
    description: "",
});
const router = useRoute();
const taskId = ref(router.params.id);
const taskName = ref(router.params.name);
const formRef = ref(null);
const editFormRef = ref(null);
const running = ref(false);
const onceRunning = ref(true);
const editVisible = ref(false);
const sentence = ref("");
const label = ref("");
const description = ref("");
const labelOptionsList = ref([]);
const uploadDateTime = computed(() => {
    let str = `上传时间：${state.uploadDateTime}，`;

    return str;
});
const state = reactive({
    exitParseTask: false,
    labelCount: 0,
    testDataCount: 0,
    testDataTypeCount: 0,
    unlabelCount: 0,
    uploadDateTime: "",
});
let baseaArr = [
    {
        title: "序号",
        dataIndex: "index",
        slotName: "rowIndex",
        width: "80",
    },
];
const baseColumns = computed(() => {
    let arr = [...baseaArr];

    switch (active.value) {
        case 2:
            arr.push({
                title: "语料问法",
                dataIndex: "sentence",
            });
            break;
        case 4:
            arr.push(
                {
                    title: "语料问法",
                    dataIndex: "sentence",
                },
                {
                    title: "标签",
                    dataIndex: "labelDes",
                }
            );
            break;
        case 8:
            arr.push(
                {
                    title: "语料问法",
                    dataIndex: "sentence",
                },
                {
                    title: "标签",
                    dataIndex: "labelDes",
                },
                {
                    title: "操作",
                    slotName: "operation",
                    fixed: "right",
                    width: "80",
                }
            );
            break;
        case 3:
            arr = [baseaArr[0]];
            arr.push({
                title: "标签 id",
                dataIndex: "label",
            });
            arr.push(
                {
                    title: "标签",
                    dataIndex: "labelDes",
                },
                ...[
                    {
                        title: "标签说明",
                        dataIndex: "description",
                        width: "280",
                    },
                    {
                        title: "示例说明",
                        dataIndex: "example",
                        width: "280",
                    },
                    {
                        title: "更新时间",
                        dataIndex: "updateDatetime",
                        width: "180",
                    },
                    {
                        title: "操作",
                        slotName: "operation",
                        fixed: "right",
                        width: "80",
                    },
                ]
            );
            break;
    }
    return arr;
});
const getData = async () => {
    let res = await getDatasetDetail({
        taskId: taskId.value,
        ...pagination,
        dataType: active.value,
        sentence: sentence.value,
        description: description.value,
        labelId: label.value,
    });
    data.value = res.data;
    pagination.total = res.total;
};

const getSummaryDataFunc = async () => {
    let res = await getSummaryData({
        taskId: taskId.value,
    });
    Object.assign(state, res.data);
    running.value = res.data.exitParseTask;
    onceRunning.value = !res.data.unlabelCount;
};
const addDataRef = ref(null);
const uploadData = () => {
    addDataRef.value.init();
};
const change = (e) => {
    active.value = e;
    pagination.current = 1;
    reset();
};
const editHandleOk = async (done) => {
    editFormRef.value.validate(async (valid) => {
        console.log(valid);
        if (!valid) {
            try {
                if (active.value == 8) {
                    let res = await updateDatasetLabel({
                        id: editform.id,
                        labelId: editform.label,
                        taskId: taskId.value,
                        dataType: 8,
                    });
                } else {
                    let res = await updateTagSet({
                        id: editform.id,
                        labelId: editform.label,
                        description: editform.description,
                        example: editform.example,
                    });
                }

                Message.success({
                    content: "修改成功！",
                });
                editVisible.value = false;
                getData();
                done();
            } catch (error) {
                console.log(error);
                done(false);
            }
        } else {
            done(false);
        }
    });
};
const addOk = () => {
    getSummaryDataFunc();
};
const handleCancel = () => {
    editVisible.value = false;
};
const reset = () => {
    sentence.value = "";
    label.value = "";
    description.value = "";
    handleSearch();
    getData();
};
const getLabelOptionsList = (val) => {
    return getLabelOptions({
        taskId: taskId.value,
        labelDesc: val,
        pageNum: 1,
        pageSize: 200,
    });
};
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
const edit = (val) => {
    editVisible.value = true;
    Object.assign(editform, val);
    editform.example = val.example;
    editform.label = val.label;
    editform.description = val.description;
};
const onceFunc = async () => {
    onceRunning.value = true;
    try {
        let res = await textOneClick({
            taskId: taskId.value,
        });
        onecClick();
        onceRunning.value = false;
    } catch (error) {
        onceRunning.value = false;
    }
};
const onecClick = inject("onecClick");
const labelSelectOptions = ref([]);
const getAll = async () => {
    let res = await getLabelOptions({
        taskId: taskId.value,
        pageNum: 1,
        pageSize: 200,
    });
    labelSelectOptions.value = res.data;
};
getAll();
getData();
getSummaryDataFunc();
emitter.on("dataset_parse", (data) => {
    
    if (data.taskId == taskId.value) {
        editVisible.value = false;
        getData();
        getSummaryDataFunc();
    }
});

onUnmounted(() => {
    emitter.off("dataset_parse");
});
</script>
<style lang='less' scoped>
</style>