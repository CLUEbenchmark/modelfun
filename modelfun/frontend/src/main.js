if (typeof window.global === 'undefined') {
	window.global = window
  }
import { createApp } from 'vue'
import App from './App.vue'
import router from '@/router/index'
import globalComponents from '@/components';				//全局样式
import directive from '@/directive/index1.js';						//自定义指令
import ArcoVue from '@arco-design/web-vue';                 //arco
import ArcoVueIcon from '@arco-design/web-vue/es/icon';     //arcoIcon
import '@arco-design/web-vue/dist/arco.css';                //arco 样式
import '@/style/global.less';
createApp(App)
.use(ArcoVue)
.use(router)
.use(globalComponents)
.use(ArcoVueIcon)
.use(directive)
.mount('#app')