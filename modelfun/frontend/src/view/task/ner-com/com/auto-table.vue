<template>
    <a-space wrap>
        <span>问法语料：</span>
        <a-input v-model="sentence" placeholder=" 请输入关键词进行搜索" style="width:220px"></a-input>
        <span>标签：</span>
        <a-select v-model="labelId" @search="handleSearch" allow-search :loading="loading" :filter-option="false" style="width:200px" placeholder="请输入想要选择的标签">
            <a-option v-for="(item,index) in labelOptionsList" :key="index" :value="item.label">{{item.labelDes}}</a-option>
        </a-select>
        
        <a-button type="primary" @click="getData">查找</a-button>
        <a-button type="primary" @click="reset">重置</a-button>
    </a-space>
    <a-table style="margin-top:16px" :columns="columns" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
        <template #rowIndex="{ rowIndex }">
            {{rowIndex+1}}
        </template>
        <template #sentence="{ record }">
            <span v-html="sentenceComputed(record)"></span>
        </template>
        <template #label="{ record }">
            <span v-html="label(record)"></span>
        </template>
        <template #operation="{record}">
            <a-button type="text" @click="edit(record)">
                编辑
            </a-button>
        </template>
    </a-table>
    <labelPop ref="labelPopRef" @updateData="updateData" saveText="保存并同步"></labelPop>

</template>
<script setup>
import labelPop from "./label-pop.vue";
import { useRoute } from "vue-router";
import { ref, computed, onUnmounted } from "vue";
import { useDebounceFn } from "@vueuse/core";
import { Message } from "@arco-design/web-vue";
import usePage from "@/hooks/usePage.js";
import emitter from "@/utils/emitter.js";
import { getNerAutoLabelList,saveAutoLabelToTrain } from "@/api/task/ner/autoLabel.js";
import { getLabelOptions } from "@/api/task/ner/dataSet.js";

import useLoading from "@/hooks/loading";
const { loading, setLoading } = useLoading();
const sentence = ref('')
const labelId = ref('')
const data = ref([]);
const { pagination, pageChange, pageSizeChange } = usePage(() => getData());
const router = useRoute();
const taskId = ref(router.params.id);
const taskName = ref(router.params.name);
const labelPopRef = ref(null);
const labelOptionsList = ref([]);
const edit = (record) => {
    labelPopRef.value.init(record);
};
const columns = ref([
    {
        title: "序号",
        dataIndex: "index",
        width: "80",
        slotName: "rowIndex",
    },
    {
        title: "语料问法",
        slotName: "sentence",
    },
    {
        title: "标签名称",
        slotName: "label",
        width: "180",
    },
    {
        width: "100",
        title: "操作",
        slotName: "operation",
    },
]);
let color = [
    "F53F3F",
    "F7BA1E",
    "D91AD9",
    "F77234",
    "3491FA",
    "00B42A",
    "F77234",
];
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
const sentenceComputed = computed(() => {
    return (v) => {
        let arr = v.sentence.split("");
        let result = [];
        let colorMap = [];
        v.labels.map((val) => {
            if (!colorMap.find((item) => item.labelId == val.labelId)) {
                colorMap.push({
                    labelId: val.labelId,
                    color: color[colorMap.length],
                });
            }
        });
        v.labels.map((val, i) => {
            let num = val.endOffset - val.startOffset;
            let flag = arr.splice(val.startOffset, num);
            let result = `<span  style='color:#${
                colorMap.find((x) => x.labelId == val.labelId).color
            }'>${flag.join("")}<span class="unitSPan">[${
                val.labelDes
            }]</span></span>`;
            for (let index = 0; index < num; index++) {
                if (index == 0) {
                    arr.splice(val.startOffset, 0, result);
                } else {
                    arr.splice(val.startOffset + 1, 0, "");
                }
            }
        });
        return arr.join("");
    };
});
const label = computed(() => {
    return (v) => {
        let result = [];
        let colorMap = [];
        v.labels.map((val) => {
            if (!colorMap.find((item) => item.labelId == val.labelId)) {
                colorMap.push({
                    labelId: val.labelId,
                    color: color[colorMap.length],
                });
            }
            if (!result.find((x) => x.labelId == val.labelId)) {
                result.push(val);
            }
        });
        let arr = result.map((v, i) => {
            return `<span style='color:#${color[i]}'>${v.labelDes}</span>`;
        });
        return arr?.join("，");
    };
});
const updateData =async (obj) => {
    let record
    data.value.map(x=>{
        if (x.id==obj.id) {
            x.labels = obj.labels;
            record=x
        }
    })
    let res = await saveAutoLabelToTrain(taskId.value,{
        dataType:props.dataType,
        ...record})
    Message.success('保存成功！')
    getData()
};
const reset = () => {
    labelId.value = "";
    sentence.value = "";
    handleSearch();
    getData();
};
const getData = async () => {
    const res = await getNerAutoLabelList({
        taskId: taskId.value,
        dataType:props.dataType||1,
        sentence:sentence.value,
        labelId:labelId.value,
        ...pagination,
    });
    data.value = res.data;
    pagination.total = res.total;
};
getData();
const props = defineProps(['dataType'])
emitter.on("auto_label", (data) => {
    if (data.taskId == taskId.value) {
        reset();
        labelPopRef?.value?.cencel();
    }
});
emitter.on("click", (data) => {
    if (data.taskId == taskId.value) {
        reset();
        labelPopRef?.value?.cencel();
    }
});
onUnmounted(() => {
    emitter.off("auto_label");
    emitter.off("click");
});
</script>
<style lang='less' scoped>
</style>