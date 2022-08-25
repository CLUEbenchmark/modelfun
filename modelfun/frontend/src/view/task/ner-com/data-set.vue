<template>
    <a-tabs :active-key="active" @change="change" lazy-load>
        <template #extra>
            <a-space>
                <span v-if="active==2">{{state.uploadDateTime?'上传时间：'+state.uploadDateTime+'，':''}}未标注量：{{state.unlabelCount}}条</span>
                <span v-else-if="active==3">{{state.uploadDateTime?'上传时间：'+state.uploadDateTime+'，':''}}标签类别：{{state.labelCount}}类</span>
                <!-- TODO 测试集数量 -->
                <span v-else-if="active==4">{{state.uploadDateTime?'上传时间：'+state.uploadDateTime+'，':''}}训练集数量：{{state.trainDataCount}}条</span>
                <span v-else-if="active==5">{{state.uploadDateTime?'上传时间：'+state.uploadDateTime+'，':''}}测试集数量：{{state.testDataCount}}条</span>
                <a-button type="primary" v-cantShow @click="uploadData" :loading="running">{{running?'正在解析':'上传数据集'}}</a-button>
                <!-- <a-button type="primary" @click="onceClickFunc" :loading="oneceRunning">{{oneceRunning?'正在标注':'一键标注'}}</a-button> -->
            </a-space>
        </template>
        <a-tab-pane :key="2" title="未标注数据集">
            <unlabeledSet />
        </a-tab-pane>
        <a-tab-pane :key="4" title="训练集">
            <trainData />
        </a-tab-pane>
        <a-tab-pane :key="5" title="测试集">
            <testData />
        </a-tab-pane>
        <a-tab-pane :key="3" title="标签集">
            <labelData />
        </a-tab-pane>
    </a-tabs>
    <a-modal v-model:visible="visible" title-align='start' @before-ok="handleOk" @cancel="handleCancel" ok-text='保存'>
        <template #title>上传数据集</template>
        <a-form :model='form' ref="formRef" auto-label-width v-if="visible">
            <a-form-item field="file" label="上传数据集：" :rules="[{required:true,message:'请上传文件'}]" :validate-trigger="['blur']">
                <a-upload style="width:370px" @before-upload="beforeUpload" :show-cancel-button="false" :default-file-list="form.fileList" :limit="1" :custom-request="customRequest">
                    <template #upload-button>
                        <a-space>
                            <a-button type="primary">
                                <template #icon>
                                    <icon-upload />
                                </template>
                                点击上传
                            </a-button>
                            <a-link :href="templateHref" @click.stop>下载模板</a-link>
                        </a-space>
                    </template>
                </a-upload>
            </a-form-item>
            <a-form-item label="上传模板：" field="mode" :rules="[{required:true,message:'请选中上传模板'}]" :validate-trigger="['blur']">
                <a-radio-group v-model="form.mode">
                    <a-radio :value="1">模板1-简洁版</a-radio>
                    <a-radio :value="2">模板2-CLUE版</a-radio>
                    <a-radio :value="3">模板3-零起步</a-radio>
                </a-radio-group>
            </a-form-item>
            <a-form-item content-flex>
                <div><span style="color:red">提示：</span>
                    <div>1. 文件上传以zip压缩包的形式上传；</div>
                    <div>2. 压缩包中包括unlabeled_data、traindata、testdata和nerdata文件，其中unlabeled_data和nerdata为必传文件；</div>
                    <div>3. 文件上传格式符合所选模板类型，否则会上传失败；</div>
                </div>
            </a-form-item>
        </a-form>
    </a-modal>
</template>
<script setup>
import { ref, computed, reactive, onUnmounted } from "vue";
import unlabeledSet from "./data-set-com/unlabeled-data.vue";
import labelData from "./data-set-com/label-data.vue";
import testData from "./data-set-com/test-data.vue";
import trainData from "./data-set-com/train-data.vue";
import {
    uploadNerDataSet,
    oneClickTrain,
    getSummaryData,
} from "@/api/task/ner/dataSet.js";
import { useRoute } from "vue-router";
import ossUpload from "@/utils/oss.js";
import { Message } from "@arco-design/web-vue";
import JSZip from "jszip";
import emitter from "@/utils/emitter.js";
const oneceRunning = ref(false);
const active = ref(2);
const visible = ref(false);
const form = reactive({
    file: "",
    fileList: [],
    mode: 1,
});
const router = useRoute();
const taskId = ref(router.params.id);
const taskName = ref(router.params.name);
const formRef = ref(null);
const running = ref(false);
const handleCancel = () => {
    visible.value = false;
};
const templateHref = computed(() => {
    let base = "/";
    return `${base}ner/数据集模板.zip`;
});
const state = reactive({
    exitParseTask: false,
    exitClickTask: false,
    labelCount: 0,
    testDataCount: 0,
    testDataTypeCount: 0,
    unlabelCount: 0,
    uploadDateTime: "",
});

const getSummaryDataFunc = async () => {
    let res = await getSummaryData({
        taskId: taskId.value,
    });
    Object.assign(state, res.data);
    running.value = res.data.exitParseTask;
    oneceRunning.value = res.data.exitClickTask;
};
const onceClickFunc = async () => {
    let res = await oneClickTrain({
        taskId: taskId.value,
    });
    Message.success({
        content: "开始一键标注",
    });
    oneceRunning.value = true;
};
const uploadData = () => {
    visible.value = true;
    form.mode = 1;
    form.file = "";
    form.fileList = [];
};
const change = (e) => {
    active.value = e;
};
const beforeUpload = async (file) => {
    var testmsg = file.name.substring(file.name.lastIndexOf(".") + 1);
    let extension = testmsg === "zip";
    if (!extension) {
        Message.error({
            content: "上传文件只能是zip格式!",
        });
        return extension;
    }
    let arr = [];
    try {
        arr = await JSZip.loadAsync(file);
    } catch (error) {
        Message.error({
            content: "zip包解析失败!",
        });
        return false;
    }
    let nameArr = [
        {
            name: "unlabeled_data",
            has: false,
            error: false,
        },
        {
            name: "nerdata",
            has: false,
            error: false,
        },
        {
            name: "testdata",
            has: false,
            error: false,
        },
        {
            name: "nerdata",
            has: false,
            error: false,
        },
    ];
    for (const key in arr.files) {
        nameArr.map((item) => {
            if (
                arr.files[key].name.indexOf(item.name) > -1 &&
                !arr.files[key].dir
            ) {
                if (item.has) {
                    item.error = true;
                } else {
                    item.has = true;
                }
            }
        });
    }
    extension =
        nameArr.filter((val, i) => {
            if (i > 1) {
                return val.error;
            }
            return !val.has || val.error;
        }).length == 0;
    if (!extension) {
        Message.error({
            content: "zip中文件命名错误",
        });
    }
    return extension;
};
const handleOk = async (done) => {
    formRef.value.validate((valid) => {
        if (!valid) {
            try {
                let res = uploadNerDataSet({
                    path: form.file,
                    taskId: taskId.value,
                    fileType: form.mode,
                });
                Message.success({
                    content: "上传成功",
                });
                visible.value = false;
                getSummaryDataFunc();
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

const customRequest = async (option) => {
    const { onError, onSuccess, fileItem, name } = option;
    console.log(option);
    let file = fileItem.file;
    try {
        const result = await ossUpload({
            taskId: taskId.value,
            type: "dataset",
            fileName: fileItem.name,
            file: file,
        });
        onSuccess(result);
        form.file = result.data;
    } catch (error) {
        onError();
    }
};
getSummaryDataFunc();
emitter.on("click", (data) => {
    if (data.taskId == taskId.value) {
        oneceRunning.value = false;
    }
});
emitter.on("dataset_parse", (data) => {
    if (data.taskId == taskId.value) {
        getSummaryDataFunc();
    }
});
onUnmounted(() => {
    emitter.off("click");
    emitter.off("dataset_parse");
});
</script>
<style lang='less' scoped>
</style>