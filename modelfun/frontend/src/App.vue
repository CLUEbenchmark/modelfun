<template>
    <router-view />
    
</template>

<script >
export default {
    name: "App",
    created() {},
    data() {
        return {
            visible: false
        }
    },
    methods:{
        handleCancel(){
            this.visible = false
        }
    }
};
</script>
<style lang="less">
#app {
    background: #eee;
}
.contentClass {
    box-shadow: 0 0 10px gray;
    width: 600px;
    max-width: 550px !important;
}

</style>