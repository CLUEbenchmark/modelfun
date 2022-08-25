import service from '@/utils/request'
/**根据任务id分页查询规则集成结果 */
export function getIntegrationList(data) {
    return service({
        url: '/integration/label/result',
        method: 'post',
        data
    })
}
//集成规则
export function IntegrationRule(data) {
    return service({
        url: '/integration/integrate',
        method: 'post',
        data
    })
}
//根据任务ID判断是否有集成任务正在运行
export function getIntegrationRunning(data) {
    return service({
        url: `/integration/running/${data.taskId}`,
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
        url: `/integration/auto_label`,
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
//删除标注结果
export function delAutoLabel(data) {
    return service({
        url: `/integration/del/auto_label`,
        method: 'post',
        data
    })
}
//修改标注结果
export function editAutoLabel(data) {
    return service({
        url: `/integration/edit/auto_label`,
        method: 'post',
        data
    })
}