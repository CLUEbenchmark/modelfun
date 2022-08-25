import service from '@/utils/request'
/**获取模型训练列表 */
export function getTrainList(data) {
    return service({
        url: '/train/list',
        method: 'post',
        data
    })
}
//开始模型训练
export function startTrain(data) {
    return service({
        url: '/train/model_train',
        method: 'post',
        data
    })
}
//根据任务ID判断是否有训练任务正在运行
export function getTrainRunning(data) {
    return service({
        url: `/train/running/${data.taskId}`,
        method: 'get',
    })
}