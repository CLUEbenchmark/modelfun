import service from '@/utils/request'
/**获取模型训练列表 */
export function getRuleList(data) {
    return service({
        url: `/rule/list/${data.taskId }`,
        method: 'get'
    })
}
/**添加规则 */
export function ruleAdd(data) {
    return service({
        url: `/rule/add`,
        method: 'post',
        data
    })
}
/**删除规则 */
export function ruleDelete(data) {
    return service({
        url: `/rule/delete`,
        method: 'post',
        data
    })
}
/**修改规则 */
export function ruleUpdate(data) {
    return service({
        url: `/rule/update`,
        method: 'post',
        data
    })
}
/**获取概率*/
export function getOverView(data) {
    return service({
        url: `/rule/overview/${data.taskId }`,
        method: 'get',
    })
}
//根据任务ID获取专家知识列表
export function getExpertList(data) {
    return service({
        url: `/rule/expert/${data.taskId }/list`,
        method: 'get',
    })
}
//测试方法
export function testRule(data) {
    return service({
        url: `/rule/lf/test`,
        method: 'post',
        data
    })
}
//获取标签options
export function getLabelOptions(data) {
    return service({
        url: `/rule/label/page`,
        method: 'post',
        data
    })
}
//根据任务ID获取正在运行的规则
export function getRunningRule(data) {
    return service({
        url: `/rule/running/${data.taskId }`,
        method: 'get',
    })
}
//获取规则下标签和测试集不一致的数据
export function getMistakeList(data) {
    return service({
        url: `/rule/lf/mistake_list`,
        method: 'post',
        data
    })
}
//获取是否有专家知识正在解析
export function getExpertRunning(data) {
    return service({
        url: `/rule/is_exist_expert_task`,
        method: 'post',
        data
    })
}
//获取高频词汇
export function getHighFrequency(data) {
    return service({
        url: `/rule/label_hf_word`,
        method: 'post',
        data
    })
}
//获取未覆盖问法
export function getUncovered(data) {
    return service({
        url: `/rule/unCoverage_list`,
        method: 'post',
        data
    })
}
//gtp 模块测试方法
export function gtpRuleFunc(data) {
    return service({
        url: `/rule/gpt_test`,
        method: 'post',
        data
    })
}
//模式匹配测试
export function regRuleFunc(data) {
    return service({
        url: `/rule/regex_test`,
        method: 'post',
        data
    })
} 
//外部系统测试
export function openapiRuleFunc(data) {
    return service({
        url: `/rule/openapi_test`,
        method: 'post',
        data
    })
} 