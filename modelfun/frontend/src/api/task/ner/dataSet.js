import service from '@/utils/request'
/**上传NER数据集 */
export function uploadNerDataSet(data) {
    return service({
		url: '/oss/upload/nerr',
        method: 'post',
        data
    })
}
/**NER一件标注接口 */
export function oneClickTrain(data) {
    return service({
		url: '/intelligent/one_click_train',
        method: 'post',
        data
    })
}
/**NER获取测试集接口 */
export function getTestSet(data) {
    return service({
        url: '/ner/testdata/list',
        method: 'post',
        data
    })
}
/**NER修改数据集的标签 */
export function updateDataSet(taskId,data) {
    return service({
        url: `/ner/update/${taskId}/datalabel`,
        method: 'post',
        data
    })
}
/**NER获取测试集接口 */
export function getTrainSet(data) {
    return service({
        url: '/ner/traindata/list',
        method: 'post',
        data
    })
}
//获取数据集总览
export function getSummaryData(data) {
    return service({
        url: '/dataset/summary',
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
/**获取根据类型获取数据集数据 */
export function getDatasetDetail(data) {
    return service({
        url: '/dataset/detail',
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