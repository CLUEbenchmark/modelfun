<template>
    <a-modal v-model:visible="visible" title-align='start' @before-ok="handleOk" @cancel="handleCancel" ok-text='保存'>
        <template #title>上传专家知识</template>
        <a-form v-if="visible" :model='form' ref="formRef" auto-label-width>
            <a-form-item field="file" label="上传数据集：" :rules="[{required:true,message:'请上传文件'}]" :validate-trigger="['blur']">
                <a-upload style="width:370px" @before-upload="beforeUpload" :show-cancel-button="false" :default-file-list="form.fileList" :limit="1" :custom-request="customRequest" >
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
                    <div>1. 文件上传以zip压缩包的形式上传；</div>
                    <div>2. 压缩包中专家知识文档，要求UTF-8编码的文本文件；</div>
                    <div>3. 文件上传包含两列，第一列为关键词，第二列为标签，中间用“Tab”键隔开。示例：老师 教育；</div>
                </div>
            </a-form-item>
            <a-form-item field="type" label="上传模式：" :rules="[{required:true}]" :validate-trigger="['blur']">
                <a-radio-group v-model="form.type">
                    <a-radio value="1" :disabled="disabled">覆盖</a-radio>
                    <a-radio value="2">增加</a-radio>
                </a-radio-group>
            </a-form-item>
        </a-form>
    </a-modal>
</template>
<script setup>
import { ref, nextTick, reactive,computed } from "vue";
import { Message } from "@arco-design/web-vue";
import ossUpload from "@/utils/oss.js";
import { useRoute } from "vue-router";
import { uploadExpertKnowledge } from "@/api/task/text/dataSet.js";
import JSZip from "jszip";
const router = useRoute();
const formRef = ref(null);
const form = reactive({
    file: "",
    type: "1",
    fileList: [],
});
const visible = ref(false);
const disabled = ref(false);
const templateHref = computed(() => {
    let base = "/";
    return `${base}text/专家知识_模板.zip`;
});
const init = (id) => {
    visible.value = true;
    disabled.value = false;
    form.file = "";
    form.fileList = [];
    nextTick(() => {
        formRef.value.resetFields();
        if (id) {
            form.type = "2";
            disabled.value = true;
        }
    });
};
const handleOk = (done) => {
    formRef.value.validate(async (valid) => {
        if (!valid) {
            try {
                let res = await uploadExpertKnowledge({
                    path: form.file,
                    taskId: router.params.id,
                    uploadType: form.type,
                });
                Message.success({
                    content: "保存成功",
                });
                emit("updateOk");
                visible.value = false;
                done();
            } catch (error) {
                done(false);
            }
        } else {
            done(false);
        }
    });
};
const handleCancel = () => {
    visible.value = false;
};
const customRequest = async (option) => {
    const { onError, onSuccess, fileItem, name } = option;
    console.log(option);
    let file = fileItem.file;
    try {
        const result = await ossUpload({
            taskId: router.params.id,
            type: "expert",
            fileName: fileItem.name,
            file: file,
        });
        onSuccess(result);
        form.file = result.name;
    } catch (error) {
        console.log(error);
        onError();
    }
};
const beforeUpload = async (file) => {
    var testmsg = file.name.substring(file.name.lastIndexOf(".") + 1);
    const extension = testmsg === "zip";
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
    if (arr?.files?.length) {
        Message.error({
            content: "zip文件为空",
        });
        return false;
    }
    return extension;
};
const emit = defineEmits(["updateOk"]);

defineExpose({
    init,
});
</script>
<style lang='less' scoped>
</style>