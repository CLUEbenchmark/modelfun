<template>
    <a-modal v-model:visible="visible" title-align='start' width="800px" @before-ok="handleOk" @cancel="handleCancel" ok-text='保存'>
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
            <a-form-item content-flex>
                <div><span style="color:red">提示：</span>
                    <div>1. 数据上传支持json和excel两种上传格式，上传时二选一，不支持混合格式上传，
                        <ul style="margin:0">
                            <li>json格式以zip压缩包的形式上传，压缩包中包括unlabeled_data、testdata、traindata和label文件，除traindata外均为必传文件；</li>
                            <li>excel格式直接上传数据文件即可，不用压缩，四种类型数据分别放在4个sheet表中；</li>
                        </ul>
                    </div>
                    <div>2. 数据集上传完成，系统会对测试集进行随机切分两部分，
                        <ul style="margin:0">
                            <li>一部分展示出来，用来评估标注规则效果；</li>
                            <li>一部分不做展示，用来评估自动标注数据的质量</li>
                        </ul>
                    </div>
                </div>
            </a-form-item>
        </a-form>
    </a-modal>
</template>
<script setup>
import { ref, reactive, computed } from "vue";
import ossUpload from "@/utils/oss.js";
import { Message } from "@arco-design/web-vue";
import JSZip from "jszip";
import { useRoute } from "vue-router";
import { uploadDataSet } from "@/api/task/text/dataSet.js";
const router = useRoute();
const taskId = ref(router.params.id);
const visible = ref(false);
const form = reactive({
    file: "",
    type: "1",
    fileList: [],
});
const templateHref = computed(() => {
    let base = "/";
    return `${base}text/数据集模板.zip`;
});
const init = () => {
    visible.value = true;
};
const beforeUpload = async (file) => {
    var testmsg = file.name.substring(file.name.lastIndexOf(".") + 1);
    if (testmsg === "xlsx") {
        return true;
    }
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
        console.log(error);
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
            name: "testdata",
            has: false,
            error: false,
        },
        {
            name: "labeldata",
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
        nameArr.filter((val) => {
            return !val.has || val.error;
        }).length == 0;
    if (!extension) {
        Message.error({
            content: "zip中文件命名错误",
        });
    }
    return extension;
};
const handleCancel = () => {
    visible.value = false;
};
const customRequest = async (option) => {
    const {  onError, onSuccess, fileItem, name } = option;
    let file = fileItem.file;
    try {
        const result = await ossUpload({
            taskId: taskId.value,
            type: "dataset",
            file: file,
        });
        onSuccess(result);
        form.file = result.data;
    } catch (error) {
        console.log(error);
        onError();
    }
};
const formRef = ref(null);
const handleOk = async (done) => {
    formRef.value.validate((valid) => {
        if (!valid) {
            try {
                let res = uploadDataSet({
                    path: form.file,
                    taskId: taskId.value,
                });
                Message.success({
                    content: "上传成功",
                });
                visible.value = false;
                emit("addOk");
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
const emit = defineEmits(["addOk"]);
defineExpose({
    init,
});
</script>
<style lang='less' scoped>
</style>