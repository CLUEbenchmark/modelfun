import service from '@/utils/request'
/**
 * 获取任务列表
 */
export function getTaskList(data) {
    return service({
        url: '/task/list',
        method: 'post',
        data
    })
}
/**创建任务 */
export function createTask(data) {
    return service({
        url: '/task/create',
        method: 'post',
        data
    })
}
/**删除任务 */
export function deleteTask(data) {
    return service({
        url: '/task/delete',
        method: 'post',
        data
    })
}
//文本一键标注
export function textOneClick(data) {
	return service({
		url: '/intelligent/text_one_click',
		method: 'post',
		data
	})
}
//修改任务
export function updateTask(data) {
    return service({
        url: '/task/update',
        method: 'post',
        data
    })
}