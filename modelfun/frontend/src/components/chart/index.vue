<template>
  <VCharts
    v-if="renderChart"
    :option="options"
    :autoresize="autoresize"
    :style="{ width, height }"
    @click="clickFunc"
  />
</template>

<script  setup>
  import { ref, nextTick } from 'vue';
 import VCharts from 'vue-echarts';
const emit = defineEmits(['clickFunc']);
  const props = defineProps({
    options: {
      type: Object,
      default() {
        return {};
      },
    },
    autoresize: {
      type: Boolean,
      default: true,
    },
    width: {
      type: String,
      default: '100%',
    },
    height: {
      type: String,
      default: '100%',
    },
  });
  const clickFunc = (params) => {
    emit("clickFunc", params);

  };
  const renderChart = ref(false);
  // wait container expand
  nextTick(() => {
    renderChart.value = true;
  });
</script>

<style scoped lang="less"></style>
