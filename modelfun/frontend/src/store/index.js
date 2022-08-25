import { createStore } from 'vuex'
import VuexPersistence from 'vuex-persist'
import getters from './getters'
import app from './module/app.js'
import user from './module/user.js'
import socket from './module/socket.js'
const vuexLocal = new VuexPersistence({
  storage: window.localStorage,
  modules: ['app', 'user']
})

export const store = createStore({
  modules: {
    app,
    user,
    socket
  },
  // strict: true,
  getters,
  plugins: [vuexLocal.plugin]
})
