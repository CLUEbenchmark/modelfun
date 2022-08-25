<template>
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
    <a-table style="margin-top:16px" :columns="columns" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
        <template #rowIndex="{ rowIndex }">
            {{rowIndex+1}}
        </template>
        <template #operation="{record}">
            <a-button type="text" @click="edit(record)">编辑</a-button>
        </template>
    </a-table>
	<a-modal v-model:visible="editVisible" title-align='start' @before-ok="editHandleOk" @cancel="handleCancel" ok-text='保存'>
        <template #title>标签编辑</template>
        <a-form :model='editform' ref="editFormRef" auto-label-width>
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
        </a-form>
    </a-modal>
</template>
<script setup>
import useLoading from "@/hooks/loading";
import { ref,reactive,onUnmounted } from "vue";
import { useDebounceFn } from "@vueuse/core";
import { useRoute } from "vue-router";
import { Message } from "@arco-design/web-vue";
import emitter from "@/utils/emitter.js";
import usePage from "@/hooks/usePage.js";
import { getDatasetDetail,updateTagSet,getLabelOptions } from "@/api/task/ner/dataSet.js";
const { pagination, pageChange, pageSizeChange } = usePage(() => getData());
const editVisible = ref(false);
const router = useRoute();
const taskId = ref(router.params.id);
const { loading, setLoading } = useLoading();
const labelOptionsList = ref([]);
const description = ref("");
const label = ref("");
const data = ref([]);
const editform = reactive({
    label: "",
    labelDes: "",
    example: "",
    description: "",
});
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        slotName: "rowIndex",
        width: "80",
    },
    {
        title: "标签 id",
        dataIndex: "label",
    },
    {
        title: "标签",
        dataIndex: "labelDes",
    },
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
]);
const reset = () => {
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
const getData = async () => {
    let res = await getDatasetDetail({
        taskId: taskId.value,
        ...pagination,
        dataType: 3,
        description: description.value,
        labelId: label.value,
    });
    data.value = res.data;
    pagination.total = res.total;
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
    editform.description = val.description;
};
const editHandleOk = async (done) => {
    try {
        let res = await updateTagSet({
            id: editform.id,
            labelId: editform.label,
            description: editform.description,
            example: editform.example,
        });
        Message.success({
            content: "修改成功！",
        });
        getData();
        editVisible.value = false;
        done();
    } catch (error) {
        console.log(error);
        done(false);
    }
};
const handleCancel = () => {
    visible.value = false;
    editVisible.value = false;
};
getData();
handleSearch()
emitter.on("dataset_parse", (data) => {
    if (data.taskId == taskId.value) {
        reset();
        editVisible.value = false;
    }
});
onUnmounted(() => {
    emitter.off("dataset_parse");
});
</script>
<style lang='less' scoped>
</style>