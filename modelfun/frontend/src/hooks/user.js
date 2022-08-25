import { Message } from '@arco-design/web-vue';
import { store } from '@/store';
import router from '../router';
import { getToken,removeToken } from '../utils/auth';
// import {removeRouteListener} from '../utils/route-listener';
import emitter from "@/utils/emitter.js";

export default function useUser() {
    //   const userStore = useUserStore();
    const logout = async () => {
        //TODO 登出 销毁一些东西
        await store.dispatch('user/logout');
        removeToken()
        emitter.all.clear()
        // removeRouteListener()
        Message.success('登出成功');
        router.push({
            name: 'Login'
        });
    };
    const login = async (data) => {
        //TODO 登录 存一些东西
        try {
            await store.dispatch('user/login', data);
        } catch (error) {
            throw new Error(error)
        }
    }
    const isLogin = () => {
        //TODO 是否登录 判断是否使用token
        return !!getToken()
    }
    return {
        logout,
        login,
        isLogin
    };
}
