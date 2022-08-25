<template>
    <a-layout class="layout" >
        <div  class="layout-navbar">
            <NavBar />
        </div>
        <a-layout>
            <a-layout>
                <!-- <a-layout-sider  class="layout-sider" breakpoint="xl" :collapsed="collapsed" :collapsible="true" :width="menuWidth" style="padding-top:60px" :hide-trigger="true" @collapse="setCollapsed">
                    <div class="menu-wrapper">
                        <Menu />
                    </div>
                </a-layout-sider> -->
                <a-layout class="layout-content" :style="paddingStyle" >
                    <a-layout-content >
                        <router-view />
                    </a-layout-content>
                    <Footer  />
                </a-layout>
            </a-layout>
        </a-layout>
    </a-layout>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter, useRoute } from "vue-router";
import { store } from "@/store/index.js";
import NavBar from "@/components/navbar/index.vue";
import Menu from '@/components/menu/index.vue';
import Footer from "@/components/footer/index.vue";
const router = useRouter();
const route = useRoute();
const navbarHeight = `60px`;
const menuWidth = computed(() => {
    return store.getters.menuCollapse ? 48 : store.getters.menuWidth;
});
const collapsed = computed(() => {
    return store.getters.menuCollapse;
});
const paddingStyle = computed(() => {
    // const paddingLeft ={ paddingLeft: `${menuWidth.value}px` }
    const paddingLeft ={ paddingLeft: `0px` }
    const paddingTop = { paddingTop: navbarHeight }
    return { ...paddingLeft, ...paddingTop };
});
const setCollapsed = (val) => {
    store.commit("app/SET_MENU_COLLAPSE", val);
};
const visible = ref(false)
const handleCancel=()=>{
            visible.value = false
        }
</script>

<style scoped lang="less">
.video {
    position: fixed;
    bottom: 50%;
    right: 20px;
    width: 50px;
    height: 50px;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 50%;
    background: #fff;
    // 阴影
    box-shadow: 0 0 5px gray;
    z-index:99;
    cursor: pointer;
    &:hover {
        box-shadow: 0 0 8px gray;
    }
}
@nav-size-height: 60px;
@layout-max-width: 1100px;

.layout {
    width: 100%;
    height: 100%;
}

.layout-navbar {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 100;
    width: 100%;
    height: @nav-size-height;
}

.layout-sider {
    position: fixed;
    top: 0;
    left: 0;
    z-index: 99;
    height: 100%;
    transition: width 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);

    &::after {
        position: absolute;
        top: 0;
        right: -1px;
        display: block;
        width: 1px;
        height: 100%;
        background-color: var(--color-border);
        content: "";
    }

    > :deep(.arco-layout-sider-children) {
        overflow-y: hidden;
    }
}

.menu-wrapper {
    height: 100%;
    overflow: auto;
    overflow-x: hidden;

    :deep(.arco-menu) {
        ::-webkit-scrollbar {
            width: 12px;
            height: 4px;
        }

        ::-webkit-scrollbar-thumb {
            background-color: var(--color-text-4);
            background-clip: padding-box;
            border: 4px solid transparent;
            border-radius: 7px;
        }

        ::-webkit-scrollbar-thumb:hover {
            background-color: var(--color-text-3);
        }
    }
}

.layout-content {
    min-height: 100vh;
    overflow-y: hidden;
    background-color: var(--color-fill-2);
    transition: padding 0.2s cubic-bezier(0.34, 0.69, 0.1, 1);
}
</style>
