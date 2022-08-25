<template>
    <a-breadcrumb class="container-breadcrumb">
        <a-breadcrumb-item>
            <!-- <icon-apps /> -->
            <icon-reply class="goBack" @click="goBack" />
        </a-breadcrumb-item>
        <a-breadcrumb-item v-for="item in items" :key="item" @click="breadcrumbClick(item)" >
            <span :class="{canClick:item.func}">{{ item.name }}</span>
            
        </a-breadcrumb-item>
    </a-breadcrumb>
</template>

<script setup>
import router from "../../router";
  defineProps({
    items: {
        type: Array,
        default() {
            return [];
        },
    },
});
const breadcrumbClick = (item) => {
    item.func&&item.func();
};
const goBack = () => {
    router.push("/task");
};
</script>

<style scoped lang="less">
.canClick{
    cursor: pointer !important;
}
.container-breadcrumb {
    margin-bottom: 16px;
    .goBack{
      cursor: pointer;
    }
    :deep(.arco-breadcrumb-item) {
        color: rgb(var(--gray-6));

        &:last-child {
            color: rgb(var(--gray-8));
        }
    }
}
</style>
