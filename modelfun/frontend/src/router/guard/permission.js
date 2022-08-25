
import NProgress from 'nprogress'; // progress bar
import useUser from '@/hooks/user';
import useSocket from "@/hooks/socket";
const { createWebSocket, websocketclose } = useSocket();
const {isLogin} = useUser();
export default function setupPermissionGuard(router) {
  router.beforeEach(async (to, from, next) => {
    NProgress.start();
    if (to.name === 'Login') {
      next();
      NProgress.done();
      websocketclose('logout')
      return;
    }
    if (isLogin()) {
      try {
        //获取用户信息
        // await userStore.info();
        // crossroads();
        next();
        //打开websocket
        createWebSocket()
      } catch (error) {
        next({
          name: 'Login',
          query: {
            redirect: to.name,
            ...to.query,
          },
        });
        NProgress.done();
        websocketclose('logout')
      }

    } else {
      
      next({
        name: 'Login',
        query: {
          redirect: to.name,
          ...to.query,
        },
      });
      NProgress.done();
      websocketclose('logout')
    }
  });
}
