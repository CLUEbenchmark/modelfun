<template>
	<a-space wrap>
		<span>问法语料：</span>
        <a-input v-model="sentence" placeholder="请输入关键词进行搜索" style="width:200px"></a-input>
        <span>标签：</span>
        <a-select v-model="label" @search="handleSearch" allow-search :loading="loading" :filter-option="false" style="width:200px" placeholder="请输入想要选择的标签">
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
        <template #labels="{ record }">
            <span v-html="labels(record)"></span>
        </template>
        <template #operation="{record}">
            <a-button type="text" @click="edit(record)">编辑</a-button>
        </template>
    </a-table>
    <labelPop ref="labelPopRef" @updateData="updateData"></labelPop>

</template>
<script setup>
import labelPop from "../com/label-pop.vue";

import { ref, computed, reactive, onUnmounted } from "vue";
import { useDebounceFn } from "@vueuse/core";
import useLoading from "@/hooks/loading";
import emitter from "@/utils/emitter.js";
import { useRoute } from "vue-router";
import { getTrainSet,updateDataSet,getLabelOptions } from "@/api/task/ner/dataSet.js";
import usePage from "@/hooks/usePage.js";
import { Message } from "@arco-design/web-vue";
const { loading, setLoading } = useLoading();
const { pagination, pageChange, pageSizeChange } = usePage(() => getData());
const labelOptionsList = ref([]);
const data = ref([])
const labelPopRef = ref(null)
const sentence = ref('')
const label = ref('')
const router = useRoute();
const taskId = ref(router.params.id);
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
        slotName: "sentence",
    },
    {
        title: "标签",
        dataIndex: "labels",
        slotName: "labels",
        width: "180",

    },
    {
        title: "操作",
        slotName: "operation",
        fixed: "right",
        width: "80",
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
const labels = computed(() => {
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
const getLabelOptionsList = (val) => {
    return getLabelOptions({
        taskId: taskId.value,
        labelDesc: val,
        pageNum: 1,
        pageSize: 200,
    });
};
const reset = () => {
    label.value = "";
    sentence.value = "";
    handleSearch();
    getData();
};
const updateData =async (obj) => {
    let record
    data.value.map(x=>{
        if (x.id==obj.id) {
            x.labels = obj.labels;
            record=x
        }
    })

    let res = await updateDataSet(taskId.value,record)
    Message.success('修改成功！')
    getData()
};
const getData = async () => {
    let res = await getTrainSet({
        taskId: taskId.value,
        ...pagination,
        dataType: 8,
        sentence: sentence.value,
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
const edit = (record) => {
    labelPopRef.value.init(record);
};
getData()
handleSearch()
emitter.on("dataset_parse", (data) => {
    if (data.taskId == taskId.value) {
        reset();
        console.log(labelPopRef)
        labelPopRef?.value?.cencel();
    }
});
onUnmounted(() => {
    emitter.off("dataset_parse");
});
</script>
<style lang='less' scoped>

</style>