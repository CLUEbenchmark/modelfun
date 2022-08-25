import { store } from '@/store'
import emitter from '../utils/emitter';
export default {
  install(Vue) {
    Vue.directive('cantShow', {
      mounted(el, asd, ccc, ddd) {

        // 聚焦元素
        let flag = !!store.getters.cantShowList.find(x => x == store.getters.userInfo.userPhone)
        let parentNode = el.parentNode
        flag && store.getters.cantShowFlag && el.parentNode && el.parentNode.removeChild(el);
        emitter.on('cantShow', (x) => {
          console.log(flag)
          if (x) {
            flag && el.parentNode && el.parentNode.removeChild(el);
          }else{
            console.log(el)
            flag && parentNode && parentNode.appendChild(el);
          }
        })
      },
      unmounted() {
        emitter.off('cantShow')
      }
    });
  },
};
