
import { login } from '@/api/user'
import { setToken } from '@/utils/auth'
import emitter from '@/utils/emitter';
const state = {
  user:'',
  token:'',
  avatar: '//lf1-xgcdn-tos.pstatp.com/obj/vcloud/vadmin/start.8e0e4855ee346a46ccff8ff3e24db27b.png',
  cantShowList:['13143219876','17000000004'],
  cantShowFlag:true,
}
const mutations = {
  TURN_SHOW(state){
    state.cantShowFlag = !state.cantShowFlag
    emitter.emit('cantShow',state.cantShowFlag)
  },
  SET_USER(state, user) {
    state.user = user
    state.cantShowList = ['13143219876','17000000004']
    state.cantShowFlag = true

  },
  SET_TOKEN(state, token) {
    state.token = token
  }
}
const actions = {
  async logout({ commit }) {
    //TODO 登出 销毁一些东西 调用接口
     commit('SET_USER', '')
     commit('SET_TOKEN', '')
     setToken('')
  },
  async login({ commit }, data) {
    return new Promise((resolve, reject) => {
      login(data)
        .then(response => {
          // vuex 内存token
          const { token } = response.data
          commit('SET_TOKEN',token)
          setToken(token)
          //保存用户信息
          commit('SET_USER', response.data)
          resolve()
        })
        .catch(error => {
          reject(error)
        })
    })
  }
}
export default {
  namespaced: true,
  state,
  mutations,
  actions
}