import service from '@/utils/request'
/**获取模型训练列表 */
export function getDatasetDetail(data) {
    return service({
        url: '/dataset/detail',
        method: 'post',
        data
    })
}
export function getSummaryData(data) {
    return service({
        url: '/dataset/summary',
        method: 'post',
        data
    })
}
// 保存上传文件
export function uploadDataSet(data) {
    return service({
        url: '/oss/upload/dataset',
        method: 'post',
        data
    })
}
//获取任务进度
export function getTaskProgress(data) {
    return service({
        url: '/dataset/parse_progress',
        method: 'post',
        data
    })
}
//保存上传专家知识
export function uploadExpertKnowledge(data) {
    return service({
        url: '/oss/upload/expert',
        method: 'post',
        data
    })
}
//更新标签集内容
export function updateTagSet(data) {
    return service({
        url: '/dataset/update_label',
        method: 'post',
        data
    })
}