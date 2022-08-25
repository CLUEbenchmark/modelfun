<template>
    <div class="container">
        <div class="logo" @click="gotoFront('/')">
            <img alt="logo" src="@/assets/logo.png" />
            <div class="logo-text">ModelFun</div>
        </div>
        <LoginBanner />
        <div class="content">
            <div class="menu">
                <div class="menu-item">
                    <span @click="gotoFront('/achievements')">效果说明</span>
                </div>
                <div class="menu-item">
                    <span @click="gotoFront('https://www.modelfun.cn/doc')">使用说明</span>
                </div>
                <div class="menu-item">
                    <span @click="gotoFront('/contact')">预约演示</span>
                </div>
            </div>
            <div class="content-inner">
                <LoginForm v-if="viewType=='login'" @gotoRegiseter="viewType='register'" />
                <registerForm v-else @gotoLogin="viewType='login'" />
            </div>
            <div class="footer">
                <Footer />
            </div>
        </div>
    </div>
</template>

<script  setup>
import Footer from "@/components/footer/index.vue";
import LoginBanner from "./components/banner.vue";
import registerForm from "./components/register-form.vue";
import LoginForm from "./components/login-form.vue";
import { ref } from "vue";
const viewType = ref("login");
const gotoFront = (val) => {
    window.location.href = val;
};
</script>
<style lang="less" scoped>
.container {
    display: flex;
    height: 100vh;
    background: var(--color-bg-1);
    min-width: 1200px;
    .banner {
        flex: 2;
        background: linear-gradient(163.85deg, #1d2129 0%, #00308f 100%);
    }
    .content {
        position: relative;
        display: flex;
        flex: 3;
        align-items: center;
        justify-content: center;
        padding-bottom: 40px;
        position: relative;
        .menu {
            position: absolute;
            right: 0;
            top: 0;
            height: 60px;
            display: flex;
            flex-direction: row-reverse;
            line-height: 60px;
            &-item {
                width: 100px;
                span {
                    cursor: pointer;
                }
            }
        }
    }

    .footer {
        position: absolute;
        right: 0;
        bottom: 0;
        width: 100%;
    }
}

.logo {
    position: fixed;
    top: 24px;
    left: 22px;
    z-index: 1;
    display: inline-flex;
    align-items: center;
    cursor: pointer;
    z-index: 999;
    img {
        width: 43px;
        height: 43px;
    }
    &-text {
        margin-right: 4px;
        margin-left: 4px;
        color: var(--color-fill-1);
        font-size: 24px;
    }
}
</style>
