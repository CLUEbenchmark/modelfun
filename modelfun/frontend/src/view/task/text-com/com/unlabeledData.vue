<template>
    <div id="parentNode">
        <a-tabs type="capsule" :active-key="activeKey" @change="activeChange">
            <a-tab-pane key="0" title="未标注数据" v-if="porps.ruleType==1">
                <div style="margin-bottom:20px">
                    覆盖率：{{coverage}}
                </div>
                <a-table :columns="columns" :data="cData" :pagination='pagination2' @pageChange='pageChange1' @pageSizeChange='pageSizeChange1'>
                    <template #rowIndex="{ rowIndex }">
                        {{rowIndex+1}}
                    </template>
                    <template #sentence="{record}">
                        <span v-html="maskData(record)"></span>
                    </template>
                </a-table>
            </a-tab-pane>
            <a-tab-pane key="1" title="高频词汇分布">
            </a-tab-pane>
            <a-tab-pane key="2" title="未覆盖数据" style="overflow:visible">
            </a-tab-pane>
            <a-tab-pane key="3" title="错误数据">
            </a-tab-pane>
        </a-tabs>
        <a-table :columns="columns" v-if="activeKey!=0" :data="data" :pagination='pagination' @pageChange='pageChange' @pageSizeChange='pageSizeChange'>
            <template #rowIndex="{ rowIndex }">
                {{rowIndex+1}}
            </template>
            <template #hfWord="{rowIndex}">
                <span v-html="nowData[rowIndex]?.hfWord"></span>
            </template>
        </a-table>
    </div>
</template>
<script setup>
import { reactive, ref, watch, h, computed } from "vue";
import usePage from "@/hooks/usePage.js";
import ruleOptions from "./rule";

import {
    getHighFrequency,
    getUncovered,
    getMistakeList,
    getDataMatchList,
} from "@/api/task/text/ruleSet.js";
import { useRoute } from "vue-router";
const data = ref([]);
const { pagination, pageChange, pageSizeChange } = usePage(() => getData(), {
    showJumper: false,
    showPageSize: false,
});
const router = useRoute();
const activeKey = ref("1");
const maskData = computed(()=>{
    return (val)=>{
        let target = false
        let str = val.sentence
        ruleList.value.map(item=>{
            if (target)return
            item.map(x=>{
                if (target)return
                if (x.ruleType=='3') {
                    let regex = new RegExp(x.regex)
                    target=regex.test(val.sentence)
                    if (target) {
                        let text = regex.exec(val.sentence)[0]
                        str=str.replace(text,`<span style="color:red">${text}</span>`)
                    }
                }else if(x.ruleType=='0'&&x.include=='0'){
                    target=val.sentence.indexOf(x.keyword)>-1
                    if (target) {
                        str=str.replace(x.keyword,`<span style="color:red">${x.keyword}</span>`)
                    }
                }
            })
        })
        return str
    }
})
const columns = computed(() => {
    switch (activeKey.value) {
        case "0":
            return [
                {
                    title: "序号",
                    dataIndex: "index",
                    width: "80",
                    slotName: "rowIndex",
                },
                {
                    title: "语料问法",
                    dataIndex: "sentence",
                    slotName: "sentence",

                },
            ];
        case "1":
            return [
                {
                    title: "序号",
                    dataIndex: "index",
                    width: "80",
                    slotName: "rowIndex",
                },
                {
                    title: "标签id",
                    dataIndex: "labelId",
                    width: "80",
                },
                {
                    title: "标签名称",
                    dataIndex: "labelDes",
                    slotName: "labelDes",
                },
                {
                    title: "高频词汇统计",
                    dataIndex: "hfWord",
                    slotName: "hfWord",
                },
            ];
        case "2":
            return [
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
                    title: "标签",
                    dataIndex: "originLabel",
                },
            ];
        case "3":
            return [
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
                    title: "原始标签",
                    dataIndex: "originLabel",
                },
                {
                    title: "标注标签",
                    dataIndex: "labeledLabel",
                },
            ];
    }
});
const ruleList = ref([]);
const matchList = ref([]);
const coverage =ref('0.00%');
const pagination2 = ref({
    current: 1,
    total: 0,
    pageSize: 10,
});
const pageChange1 = (pageNum) => {
    pagination2.value.current = pageNum;
    console.log(cData);
};
const pageSizeChange1 = (pageSize) => {
    pagination2.value.pageSize = pageSize;
};
const init = (val,x) => {
    ruleList.value = JSON.parse(JSON.stringify(val));
    if (x&&porps.ruleType == "1") {
        activeKey.value = "0";
    } 
    getData(1);
};

const cData = computed(() => {
    //根据页码和页数提取数据

    let data = matchList.value.slice(
        (pagination2.value.current - 1) * pagination2.value.pageSize,
        pagination2.value.current * pagination2.value.pageSize
    );
    return data;
});
defineExpose({
    init,
});
const getData = async (val) => {
    let res;
    switch (activeKey.value) {
        case "0":
            pagination2.value.current = 1;
            let obj = ruleList.value.map((x) => {
                return x.map((y) => {
                    if (y.ruleType == "1") {
                        y.regex = ruleOptions.range["&" + y.gapType](
                            y.gap,
                            y.keyword,
                            y.endKeyword
                        );
                    } else if (y.ruleType == "2") {
                        y.regex = ruleOptions.len["&" + y.lenType](y.len);
                    }
                    return y;
                });
            });
            let target = await getDataMatchList({
                taskId: router.params.id,
                ruleId: ruleId || -1,
                metadata: JSON.stringify(obj),
                label: porps.label,
                pageNum: pagination2.value.current,
                pageSize: pagination2.value.pageSize,
            });
            matchList.value = target.data.dataList;
            pagination2.value.total = matchList.value.length;
            coverage.value = target.data.coverage;
            break;
        case "1":
            res = await getHighFrequency({
                taskId: router.params.id,
                ...pagination,
            });
            data.value = res.data;
            pagination.total = res.total;
            handleNowData();
            break;
        case "2":
            res = await getUncovered({
                taskId: router.params.id,
                ruleId: ruleId || -1,
                ruleType: ruleType,
                label: label,
                ...pagination,
            });
            data.value = res.data;
            pagination.total = res.total;
            break;
        case "3":
            console.log(porps);
            res = await getMistakeList({
                taskId: router.params.id,
                ...pagination,
                ruleId: porps.ruleId || -1,
            });
            data.value = res.data;
            pagination.total = res.total;
            break;
    }
};
const nowData = ref([]);
const activeChange = (val) => {
    activeKey.value = val;
    pagination.current = 1;
    getData();
};
const randomColor = Math.floor(Math.random() * 16777215).toString(16);
console.log(randomColor);
let color = [
    "F53F3F",
    "F7BA1E",
    "D91AD9",
    "F77234",
    "3491FA",
    "00B42A",
    "F77234",
];
// let arr = [{
//     val:'',
//     add:[[1,1]]
// }]

const handleNowData = () => {
    let arr = [];
    nowData.value = JSON.parse(JSON.stringify(data.value));
    nowData.value?.map((val, x) => {
        let hfWord = val.hfWord?.split(",");
        val.hfWord = hfWord;
        hfWord?.map((item, y) => {
            //arr中是否含有item
            let obj = arr.find((c) => c.val === item);
            if (obj) {
                obj.add.push([x, y]);
            } else {
                arr.push({
                    val: item,
                    add: [[x, y]],
                });
            }
        });
    });
    arr.map((val, i) => {
        let colorItem =
            color[i] || Math.floor(Math.random() * 16777215).toString(16);
        val.add.length > 1 &&
            val.add.map((item, j) => {
                nowData.value[item[0]].hfWord[item[1]] =
                    `<span style="color:#${colorItem};">` + val.val + "</span>";
            });
    });
    nowData.value.map((x) => {
        x.hfWord = x?.hfWord?.join(",");
    });
};
watch(
    () => {
        return porps.ruleType;
    },
    (val, oldVal) => {
        console.log(oldVal);
        if (oldVal == 1) {
            activeKey.value = "1";
        }
    }
);
const porps = defineProps(["ruleId", "ruleType", "label"]);
const { ruleId, ruleType, label } = porps;
</script>
<style lang='less' scoped>
.yellow {
    background: red;
    color: red;
}
#parentNode {
    position: relative;
    overflow: visible !important;
    min-height: 530px;
}
</style>
<style>
.search_yellow {
    background: yellow;
}
</style>