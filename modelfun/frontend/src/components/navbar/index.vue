<template>
    <div class="navbar">
        <div class="left-side">
            <a-space>
                <img alt="logo" @click="add" class="logo" src="@/assets/logo.png" />
                <a-typography-title :style="{ margin: 0, fontSize: '18px' }" :heading="5">
                    Model Fun
                </a-typography-title>
            </a-space>
        </div>
        <ul class="right-side">
            <li>
                <a-dropdown trigger="click">
                    <a-avatar :size="32" :style="{ marginRight: '8px' }">
                        <img alt="avatar" :src="avatar" />
                    </a-avatar>
                    <template #content>
                        <a-doption>
                            <a-space @click="handleLogout">
                                <icon-export />
                                <span>
                                    登出登录
                                </span>
                            </a-space>
                        </a-doption>
                    </template>
                </a-dropdown>
            </li>
        </ul>
    </div>
</template>

<script  setup>
import { computed, ref } from "vue";
import { Message } from "@arco-design/web-vue";
import { store } from "@/store";
import  useUser from "@/hooks/user";
const avatar = computed(() => {
    return store.getters.avatar;
  });
const device = computed(() => {
    return store.getters.device;
});
const { logout } = useUser();
const handleLogout = ()=>{
    logout()
}
const num=ref(0)
const add = () => {
    num.value++
    if (num.value%5==0) {
        console.log(store)
        store.commit('user/TURN_SHOW')
    }
};
</script>

<style scoped lang="less">
.navbar {
    display: flex;
    justify-content: space-between;
    height: 100%;
    background-color: var(--color-bg-2);
    border-bottom: 1px solid var(--color-border);
}

.left-side {
    display: flex;
    align-items: center;
    padding-left: 20px;
}

.right-side {
    display: flex;
    padding-right: 20px;
    list-style: none;

    :deep(.locale-select) {
        border-radius: 20px;
    }

    li {
        display: flex;
        align-items: center;
        padding: 0 10px;
    }

    a {
        color: var(--color-text-1);
        text-decoration: none;
    }

    .nav-btn {
        color: rgb(var(--gray-8));
        font-size: 16px;
        border-color: rgb(var(--gray-2));
    }

    .trigger-btn,
    .ref-btn {
        position: absolute;
        bottom: 14px;
    }

    .trigger-btn {
        margin-left: 14px;
    }
}
</style>

<style lang="less">
.message-popover {
    .arco-popover-content {
        margin-top: 0;
    }
}
.logo{
    width: 33px;
    height: 33px;
}
</style>
