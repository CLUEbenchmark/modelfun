<template>
    <div class="login-form-wrapper">
        <div class="login-form-title">注册 ModelFun 账号</div>
        <div class="login-form-sub-title">注册 ModelFun 账号</div>
        <div class="login-form-error-msg">{{ errorMessage }}</div>
        <a-form ref="loginForm" :model="userInfo" class="login-form" layout="vertical" @submit="handleSubmit">
            <a-form-item field="username" :rules="[{ required: true, message:'手机号不能为空'}]" :validate-trigger="['change', 'blur']" hide-label>
                <a-input v-model="userInfo.username" :placeholder="'请输入手机号'">
                    <template #prefix>
                        <icon-user />
                    </template>
                </a-input>
            </a-form-item>
            <a-form-item :rules="[{ required: true, message: '短信验证码不能为空' }]" :validate-trigger="['change', 'blur']" hide-label>
                <a-input v-model="userInfo.password" placeholder="请输入短信验证码" allow-clear>
                    <template #prefix>
                        <icon-message />
                    </template>
                </a-input>
                <a-button @click="handleDelete(index)" :style="{marginLeft:'10px'}">获取短信验证码</a-button>
            </a-form-item>
            <a-form-item field="password" :rules="[{ required: true, message: '密码不能为空' }]" :validate-trigger="['change', 'blur']" hide-label>
                <a-input-password v-model="userInfo.password" :placeholder="'请输入密码'">
                    <template #prefix>
                        <icon-lock />
                    </template>
                </a-input-password>
            </a-form-item>
            <a-space :size="16" direction="vertical">
                <div class="login-form-password-actions">
                </div>
                <a-button type="primary" html-type="submit" long :loading="loading">
                    注册并登陆
                </a-button>
                <a-button type="text" long class="login-form-register-btn" @click="emit('gotoLogin')">
                    已有账号？去登录
                </a-button>
            </a-space>
        </a-form>
    </div>
</template>

<script  setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import useLoading from "@/hooks/loading";
const emit = defineEmits(["gotoLogin"]);
const router = useRouter();
const errorMessage = ref("");
const { loading, setLoading } = useLoading();
const userInfo = reactive({
    username: "",
    password: "",
});
const handleSubmit = async ({ errors, values }) => {
    if (!errors) {
        setLoading(true);
        try {
            Message.success("登陆成功");
        } catch (err) {
            errorMessage.value = err.message;
        } finally {
            setLoading(false);
        }
    }
};
const setRememberPassword = () => {
    //
};
</script>

<style lang="less" scoped>
.login-form {
    &-wrapper {
        width: 320px;
    }

    &-title {
        color: var(--color-text-1);
        font-weight: 500;
        font-size: 24px;
        line-height: 32px;
    }

    &-sub-title {
        color: var(--color-text-3);
        font-size: 16px;
        line-height: 24px;
    }

    &-error-msg {
        height: 32px;
        color: rgb(var(--red-6));
        line-height: 32px;
    }
    &-register-btn {
        color: var(--color-text-3) !important;
    }
}
</style>
