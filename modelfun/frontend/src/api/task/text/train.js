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
export function getLabelDetail(data) {
    return service({
        url: `/train/train/label_detail`,
        method: 'post',
        data
    })
}
//获取错误数据
export function getLabelDiff(data) {
    return service({
        url: `/train/train/label_diff`,
        method: 'post',
        data
    })
}
//获取混淆矩阵
export function getConfusionMatrix(data) {
    return service({
        url: `/train/confusion_matrix`,
        method: 'post',
        data
    })
}
//获取模型下载地址
export function getModelDownload(data) {
    return service({
        url: `/train/download/report`,
        method: 'post',
        data
    })
}
//获取混淆矩阵下单个的数据
export function getMatrixDtail(data) {
    return service({
        url: `/train/matrix/detail`,
        method: 'post',
        data
    })
}
