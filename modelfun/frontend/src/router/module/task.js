let arr = [
    {
        path: '/task',
        name: 'Task',
        component: () => import('@/view/task/index.vue'),
        meta: {
            requiresAuth: true,
            locale: '任务管理',
            icon: 'icon-file'
        }
    },
    {
        path: '/text/:id/:name',
        name: 'Text',
        component: () => import('@/view/task/page-text.vue')
    },
    {
        path: '/ner/:id/:name',
        name: 'Ner',
        component: () => import('@/view/task/page-ner.vue')
    },
]
export default arr