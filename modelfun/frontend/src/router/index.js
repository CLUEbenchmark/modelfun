import { createRouter, createWebHistory } from 'vue-router'
import createRouteGuard from './guard';
import task from './module/task.js'
const routes = [
  {
    path: '/',
    redirect: '/task'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/view/login/index.vue')
  },
  {
    path: '/layout',
    name: 'layout',
    component: () => import('@/view/layout/index.vue'),
    children: [
      ...task
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('@/view/not-found/index.vue'),
  },
]
let base = '/'
const router = createRouter({
  history: createWebHistory(base),
  routes
})
createRouteGuard(router);

export default router
