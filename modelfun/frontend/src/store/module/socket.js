/**
 * websocket模块
 * 1统计事件管理
 * 2状态管理
 * ...
 */
 import emitter from '@/utils/emitter.js';
import {  Notification } from "@arco-design/web-vue";

const state = {
}
const mutations = {
}
const actions = {
  dataset_parse({ commit },obj) {
    emitter.emit('dataset_parse',obj.data);
    Notification[obj.data.success?'success':'error']({
      content:obj.data.msg,
      closable:true,
      duration:5000
    });
  },
  integrated({ commit },obj) {
    emitter.emit('integrated',obj.data);
    Notification[obj.data.success?'success':'error']({
      content:obj.data.msg,
      closable:true,
      duration:5000
    });
  },
  rule({ commit },obj) {
    emitter.emit('rule',obj.data);
    Notification[obj.data.success?'success':'error']({
      content:obj.data.msg,
      closable:true,
      duration:5000
    });
  },
  auto_label({ commit },obj) {
    emitter.emit('auto_label',obj.data);
    Notification[obj.data.success?'success':'error']({
      content:obj.data.msg,
      closable:true,
      duration:5000
    });
  },
  train({ commit },obj) {
    emitter.emit('train',obj.data);
    Notification[obj.data.success?'success':'error']({
      content:obj.data.msg,
      closable:true,
      duration:5000
    });
  },
  click({ commit },obj) {
    emitter.emit('click',obj.data);
    Notification[obj.data.success?'success':'error']({
      content:obj.data.msg,
      closable:true,
      duration:5000
    });
  },
  text_click({ commit },obj) {
    emitter.emit('text_click',obj.data);
    if (!obj.data.success) {
      Notification[obj.data.success?'success':'error']({
        content:obj.data.msg,
        closable:true,
        duration:5000
      });
    }
    
  }
}
export default {
  namespaced: true,
  state,
  mutations,
  actions
}