import service from '@/utils/request'
/**根据任务id分页查询规则集成结果 */
export function getIntegrationList(data) {
    return service({
        url: '/ner/autolabel/list',
        method: 'post',
        data
    })
}
//根据任务ID判断是否有标注任务正在运行
export function getLabelRunning(data) {
    return service({
        url: `/integration/labeling/${data.taskId}`,
        method: 'get',
    })
}
//获取集成概览
export function getIntegrationOverview(data) {
    return service({
        url: `/integration/overview`,
        method: 'post',
        data
    })
}
//开始集成
export function startIntegration(data) {
    return service({
        url: `/integration/auto_label/ner`,
        method: 'post',
        data
    })
}
//分页获取ner自动标注结果
export function getNerAutoLabelList(data) {
    return service({
        url: `/ner/autolabel/list`,
        method: 'post',
        data
    })
}
//保存自动标注数据到训练集
export function saveAutoLabelToTrain(taskId,data) {
    return service({
        url: `/ner/import/${taskId}/auto_label`,
        method: 'post',
        data
    })
}