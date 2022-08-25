import axios from 'axios' // 引入axios
import { Message, Modal } from '@arco-design/web-vue';
import { getToken } from '@/utils/auth'
import useSocket from "@/hooks/socket";
import emitter from "@/utils/emitter.js";
const { websocketclose } = useSocket();
// import JSONbig from 'json-bigint'
import router from '@/router'
const BASE_PATH = import.meta.env.VITE_BASE_PATH
const service = axios.create({
  baseURL: BASE_PATH,
  timeout: 99999
})
let isExpire = false
// http request 拦截器
service.interceptors.request.use(
  config => {
    config.headers['Authorization'] = getToken() || ''

    return config
  },
  error => {
    Message.error({
      showClose: true,
      content: error.message || error.msg || error || 'Error',
    })
    return error
  }
)
// response 白名单，用来过滤下载文件之类的接口
const responseWhiteList = [
]
// http response 拦截器
service.interceptors.response.use(
  response => {
    const { data: res } = response

    if (res.code === 0 || responseWhiteList.includes(response.config.url)) {
      return res
    } else {
      if (isExpire)
        return
      if (res.code === 401) {
        isExpire = true
        Modal.warning({
          okText: '去登录',
          title: '提示',
          content: '登录已经过期，请重新登录',
          maskClosable: false,
          onOk: () => {
            router.push({ name: 'Login' })
              .then(res => {
                emitter.all.clear()
                websocketclose('logout')
                setTimeout(() => {
                  location.reload()
                }, 50);
              })
          }
        })
      } else {
        Message.error({
          content: res.message || res.msg || 'Error',
          duration: 5 * 1000
        })
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  error => {
    Message.error({
      content: error.message || error.msg || error || 'Error',
      duration: 5 * 1000
    })
    return Promise.reject(new Error(error))
  }
)

export default service
