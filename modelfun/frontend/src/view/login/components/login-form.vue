<template>
    <div class="login-form-wrapper">
        <div class="login-form-title">登录 ModelFun</div>
        <div class="login-form-sub-title">登录 ModelFun</div>
        <div class="login-form-error-msg"></div>
        <a-form ref="loginForm" :model="userInfo" class="login-form" layout="vertical" @submit="handleSubmit">
            <a-form-item field="username" :rules="[{ required: true, message: '手机号不能为空' }]" :validate-trigger="['change', 'blur']" hide-label>
                <a-input v-model.trim="userInfo.username" :placeholder="'请输入手机号'">
                    <template #prefix>
                        <icon-user />
                    </template>
                </a-input>
            </a-form-item>
            <a-form-item field="password" :rules="[{ required: true, message: '密码不能为空' }]" :validate-trigger="['change', 'blur']" hide-label>
                <a-input-password v-model="userInfo.password" :placeholder="'请输入密码'" allow-clear>
                    <template #prefix>
                        <icon-lock />
                    </template>
                </a-input-password>
            </a-form-item>
            <a-space :size="16" direction="vertical">
                <!-- <div class="login-form-password-actions">
                    <a-checkbox checked="rememberPassword" @change="setRememberPassword">
                        记住密码
                    </a-checkbox>
                    <a-link>忘记密码</a-link>
                </div> -->
                <a-button type="primary" html-type="submit" long :loading="loading">
                    登 录
                </a-button>
                <!-- <a-button type="text" long class="login-form-register-btn" @click="emit('gotoRegiseter')">
                    注册账号
                </a-button> -->
            </a-space>
        </a-form>
    </div>
</template>

<script  setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
// import { store } from "@/store";
import useUser from "@/hooks/user";
import useLoading from "@/hooks/loading";
const emit = defineEmits(["gotoRegiseter"]);
const router = useRouter();
const errorMessage = ref("");
const { loading, setLoading } = useLoading();
const { login } = useUser();
const userInfo = reactive({
    username: import.meta.env.VITE_APP_ENV=="dev"?'13012345678':"",
    password: import.meta.env.VITE_APP_ENV=="dev"?'123456':'',
});
//记住密码
const setRememberPassword = () => {};
const handleSubmit = async ({ errors, values }) => {
    
    if (!errors) {
        setLoading(true);
        try {
            await login(values);
            router.push({name:'Task'});
            Message.success("欢迎使用");
        } catch {
        } finally {
            setLoading(false);
        }
    }
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

    &-password-actions {
        display: flex;
        justify-content: space-between;
    }

    &-register-btn {
        color: var(--color-text-3) !important;
    }
}
</style>
