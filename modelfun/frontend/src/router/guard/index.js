
import { setRouteEmitter } from '@/utils/route-listener';
import setupPermissionGuard from './permission';

function setupPageGuard(router) {
  router.beforeEach(async (to) => {
    // emit route change
    setRouteEmitter(to);
  });
}

export default function createRouteGuard(router) {
  // setupPageGuard(router);
  setupPermissionGuard(router);
}
