// import { App } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { BarChart, LineChart, PieChart, RadarChart,HeatmapChart } from 'echarts/charts';
import {
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
  GraphicComponent,
  VisualMapComponent
} from 'echarts/components';
import Chart from './chart/index.vue';
import Breadcrumb from './breadcrumb/index.vue';
import codeView from './code-view/index.vue';


use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  RadarChart,
  GridComponent,
  TooltipComponent,
  LegendComponent,
  DataZoomComponent,
  GraphicComponent,
  VisualMapComponent,
  HeatmapChart
]);

export default {
  install(Vue) {
    Vue.component('Chart', Chart);
    Vue.component('codeView', codeView);
    Vue.component('Breadcrumb', Breadcrumb);
  },
};
