<template>
    <a-modal v-model:visible="visible" title-align='start' width="800px" @before-ok="handleOk" @cancel="handleCancel" :ok-text="props.saveText||'保存'">
        <template #title>修改标签</template>
        <div ref="mian" style="position:relative;overflow: visible;">
            <p @mouseup="SelectText($event)" class="mian" id="mian" @mousedown="cilckFunc($event)">
            </p>
            <a-trigger :popup-visible="triggerVisible" :style="{top:position.y+10+'px',left:position.x+10+'px'}" :popup-translate="[100, 20]" align-point :popup-container="mian" click-outside-to-close>
                <template #content>
                    <div class="menu-list">
                        <a-select v-model="nowLabel.labelId" @change="handerSelect" @clear="clear" allow-clear placeholder="请输入想要选择的标签">
                            <a-option v-for="(item,index) in labelSelectOptions" :key="index" :value="item.label">{{item.labelDes}}</a-option>
                        </a-select>
                    </div>
                </template>
            </a-trigger>
        </div>
        <div>
            <a-space v-if="show" wrap>
                <label>已选标签：</label>
                <a-tag v-for="(item,index) in labels" :key="index" closable @close="close(index)">{{item.labelDes}}</a-tag>
            </a-space>
        </div>

    </a-modal>
</template>
<script setup>
import { ref, nextTick } from "vue";
import { useRoute } from "vue-router";
import { getLabelOptions } from "@/api/task/ner/dataSet.js";
import $ from "jquery";
const router = useRoute();
const labels = ref([]);
const visible = ref(false);
const triggerVisible = ref(false);
const mian = ref(null);
const nowLabel = ref(null);
let record;
let color = [
    "F53F3F",
    "F7BA1E",
    "D91AD9",
    "F77234",
    "3491FA",
    "00B42A",
    "F77234",
];
const show = ref(true);
const position = ref({
    x: 0,
    y: 0,
});
const taskId = ref(router.params.id);
const init = (val) => {
    triggerVisible.value = false;
    record = val;
    let arr = val.labels.sort((a, b) => {
        return a.startOffset - b.startOffset;
    });
    console.log(arr)
    labels.value = [...arr];
    visible.value = true;
    getLabelFunc();
    handleColor();
    nextTick(() => {
        $("p.mian").html(handleSentence());
    });
};
const labelSelectOptions = ref([]);
const clear = (e) => {
    labels.value.splice(nowLabel.value.index, 1);
    triggerVisible.value = false;
    nextTick(() => {
        $("p.mian").html(handleSentence());
    });
};
const colorMap = ref([]);
const handleColor = () => {
    colorMap.value = [];
    labels.value.map((val) => {
        if (!colorMap.value.find((item) => item.labelId == val.labelId)) {
            colorMap.value.push({
                labelId: val.labelId,
                color: `color${colorMap.value.length + 1}`,
            });
        }
    });
};
const getLabelFunc = async () => {
    let res = await getLabelOptions({
        taskId: taskId.value,
        pageNum: 1,
        pageSize: 1000,
    });
    labelSelectOptions.value = res.data.map((x) => {
        return {
            label: x.mapKey * 1,
            labelDes: x.mapValue,
        };
    });
    handleColor();
};
const close = (index) => {
    show.value = false;
    labels.value.splice(index, 1);
    nextTick(() => {
        console.log(index, labels.value);
        $("p.mian").html(handleSentence());
        show.value = true;
    });
};
const handerSelect = (e) => {
    if (!e && e !== 0) return;
    nowLabel.value.labelDes = labelSelectOptions.value.find(
        (x) => x.label == e
    ).labelDes;
    if (nowLabel.value.add) {
        let index = nowLabel.value.index;
        delete nowLabel.value.index;
        delete nowLabel.value.add;
        labels.value.splice(index, 0, nowLabel.value);
    } else {
        let index = nowLabel.value.index;
        delete nowLabel.value.index;
        delete nowLabel.value.add;
        labels.value[index] = nowLabel.value;
    }
    console.log(labels.value);
    handleColor();
    triggerVisible.value = false;
    nextTick(() => {
        $("p.mian").html(handleSentence());
    });
};
const handleSentence = () => {
    let str = record.sentence;
    let arr = str.split("");
    labels.value.map((val, i) => {
        let num = val.endOffset - val.startOffset;
        let flag = arr.splice(val.startOffset, num);
        let colorStr = colorMap.value.find(
            (item) => item.labelId == val.labelId
        )?.color;
        let result = `<div class='labelDiv ${colorStr}'>${flag.join(
            ""
        )}<div class="labelItem">${val.labelDes}</div></div>`;

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
const cilckFunc = (e) => {
    triggerVisible.value = false;
    let selectStr = window.getSelection().toString();
    if (selectStr.trim() == "" && e.path[0].className == "labelItem") {
        let list = e.path[2].children;
        let result = 0;
        for (let index = 0; index < list.length; index++) {
            if (list[index] === e.path[1]) {
                result = index;
            }
        }

        position.value = {
            x: e.x - e.path[5].offsetLeft,
            y: e.y - e.path[5].offsetTop - 90,
        };
        if (position.value.x > 510) {
            position.value.x = position.value.x - 250;
        }
        console.log(labels.value);
        nowLabel.value = labels.value[result];
        nowLabel.value.index = result;
        nowLabel.value.add = false;
        triggerVisible.value = true;
    }
};

const handleOk = (done) => {
    emit("updateData", {
        id: record.id,
        labels: labels.value,
    });
    done();
};
const handleCancel = () => {
    visible.value = false;
};
const props = defineProps(["saveText"]);
const SelectText = (e) => {
    setTimeout(() => {
        var selecter = window.getSelection();
        var selectStr = selecter.toString();
        if (selectStr.trim() != "") {
            var rang = selecter.getRangeAt(0);
            if (rang.startContainer.parentElement.className !== "mian") return;
            let childArr = rang.cloneContents().childNodes;
            if (childArr.length > 1) return;
            let num = 0;
            let list = rang.startContainer.parentNode.childNodes;
            let result = 0;
            for (let index = 0; index < list.length; index++) {
                if (list[index] === rang.startContainer) {
                    //结束for 循环
                    break;
                }
                if (list[index].tagName == "DIV") {
                    num += list[index].childNodes[0].length;
                    result++;
                } else {
                    num += list[index].length;
                }
            }

            position.value = {
                x: e.offsetX,
                y: e.offsetY,
            };
            if (e.offsetX > 510) {
                position.value.x = position.value.x - 250;
            }
            triggerVisible.value = true;
            nowLabel.value = {
                startOffset: num + rang.startOffset, //起始位置
                endOffset: num + rang.endOffset, //结束位置
                labelId: "",
                labelDes: "",
                add: true,
                index: result,
            };
        }
    }, 50);
};
const cencel = () => {
    visible.value = false;
};
const emit = defineEmits(["updateData"]);
defineExpose({
    init,
    cencel
});
</script>
<style lang='less' scoped>
.mian {
    width: 100%;
    min-height: 200px;
    padding: 30px;
    border: 1px solid #ccc;
    overflow: hidden;
    overflow-y: auto;
    max-height: 600px;
}
.menu-list {
    height: 46px;
    width: 250px;
    padding: 8px;
    background-color: #fff;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}
</style>
<style lang='less'>
.labelDiv {
    display: inline-block;
    margin: 0 5px;
    position: relative;
    vertical-align: top;
    text-align: center;

    &::after {
        content: "";
        position: absolute;
        left: 0;
        top: 20px;
        background: #000;
        border-radius: 2px;
        width: 100%;
        height: 4px;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
        cursor: pointer;
    }
    &.color0 {
        &::after {
            background: #f53f3f;
        }
        .labelItem {
            &::after {
                background: #f53f3f;
            }
        }
    }
    &.color1 {
        &::after {
            background: #f7ba1e;
        }
        .labelItem {
            &::after {
                background: #f7ba1e;
            }
        }
    }
    &.color2 {
        &::after {
            background: #d91ad9;
        }
        .labelItem {
            &::after {
                background: #d91ad9;
            }
        }
    }
    &.color3 {
        &::after {
            background: #f77234;
        }
        .labelItem {
            &::after {
                background: #f77234;
            }
        }
    }
    &.color4 {
        &::after {
            background: #3491fa;
        }
        .labelItem {
            &::after {
                background: #3491fa;
            }
        }
    }
    &.color5 {
        &::after {
            background: #00b42a;
        }
        .labelItem {
            &::after {
                background: #00b42a;
            }
        }
    }
    &.color6 {
        &::after {
            background: #f77234;
        }
        .labelItem {
            &::after {
                background: #f77234;
            }
        }
    }
}
.labelItem {
    text-align: left;
    position: relative;
    padding-left: 10px;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    color: #7a7a7a;
    cursor: pointer;
    &:hover {
        &::after {
            width: 7px;
            height: 7px;
        }
    }
    //字体前面添加一个圆点
    &::after {
        content: "";
        position: absolute;
        left: 0px;
        top: 9px;
        width: 5px;
        height: 5px;
        border-radius: 50%;
        background: #000;
    }
}
</style>