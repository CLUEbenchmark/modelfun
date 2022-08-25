<template>
    <a-modal v-model:visible="visible" title-align='start' @before-ok="handleOk" @cancel="handleCancel" ok-text='保存'>
        <template #title>{{id?'编辑任务':'新建任务'}}</template>
        <a-form :model='form' ref="formRef">
            <a-form-item field="name" label="任务名称" :rules="[{required:true,message:'请输入任务名称'},{maxLength:10,message:'不要超过10个字！'}]" :validate-trigger="['blur']">
                <a-input v-model="form.name" placeholder="请输入任务名称" />
            </a-form-item>
            <a-form-item label="关键词" field="keyword">
                <a-input v-model="form.keyword" placeholder="请输入关键词" />
            </a-form-item>
            <a-form-item field="description" label="任务描述">
                <a-input v-model="form.description" placeholder="请输入任务描述" />
            </a-form-item>
            <a-form-item field="domain" label="任务领域" :rules="[{required:true,message:'请输入任务领域'}]" :validate-trigger="['blur']">
                <a-input v-model="form.domain" placeholder="请输入任务领域" />
            </a-form-item>
            <a-form-item field="taskType" label="任务类型" :rules="[{required:true,message:'请选择任务类型'}]" :validate-trigger="['blur']">
                <a-select v-model="form.taskType" placeholder="请选择任务类型" :disabled="!!id">
                    <a-option :value="1">文本分类</a-option>
                    <a-option :value="2">ner</a-option>
                </a-select>
            </a-form-item>
            <a-form-item field="languageType" label="语言类型" :rules="[{required:true,message:'请选择语言类型'}]" :validate-trigger="['blur']">
                <a-select v-model="form.languageType" placeholder="请选择语言类型" :disabled="!!id">
                    <a-option :value="1">中文</a-option>
                    <a-option :value="2">英文</a-option>
                </a-select>
            </a-form-item>
        </a-form>
    </a-modal>
</template>
<script setup>
import { nextTick, ref, inject } from "vue";
import { createTask,updateTask } from "@/api/task";
import { store } from "@/store";
import { Message } from "@arco-design/web-vue";
const visible = ref(false);
const formRef = ref(null);
const findPwdRef = ref(null);
const userInfo = store.getters.userInfo;

const form = ref({
    name: "",
    keyword: "",
    description: "",
    domain: "",
    languageType: 1,
    taskType: "",
});
const handleOk = (done) => {
    formRef.value.validate(async (errors) => {
        if (!errors) {
            try {
                if (id.value) {
                    await updateTask({
                        ...form.value,
                        id: id.value,
                    })
                } else {
                    await createTask({
                        userId: userInfo.id,
                        ...form.value,
                    });
                }
                done();
                Message.success(id.value ? "编辑成功" : "创建成功");
                updateList();
            } catch (error) {
                done(false);
            }
        } else {
            done(false);
        }
    });
};
const handleCancel = () => {};
const id = ref(null);
const init = (val) => {
    visible.value = true;
    id.value = val?.id;
    let asd = Object.assign(form.value, val);
    Object.assign(form.value, val);
    nextTick(() => {
        if (!val?.id) formRef.value.resetFields();
    });
};
const updateList = inject("updateList");
defineExpose({
    init,
});
</script>