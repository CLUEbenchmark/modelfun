import { Message } from '@arco-design/web-vue'
import service from '@/utils/request'
export const getFileNameUUID = () => {
    const rx = () =>
        (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1)
    return `${+new Date()}_${rx()}${rx()}`
}
/**
 *
 * @param {*} param
 * type 文件类型 image/audio 两种，用来存放不同文件夹下  TODO 是否将文件类型判断放在该函数中
 * fileName 文件名
 * data 上传的 File/Blob/OSS Buffer内容（OSS.Buffer('hello oss')） 类型的文件
 * showErrorMsg 是否展示上传失败的提示内容
 * @returns
 */
async function putFile({
    taskId = '',
    type = 'dataset',
    file,
    showErrorMsg = true,
}) {
    if (!file || !taskId) return
    try {
        let formData = new FormData()
        formData.append('file', file)
        formData.append('taskId', taskId)
        formData.append('type', type)
        return service.post('/oss/upload',formData)
    } catch (e) {
        if (showErrorMsg) {
            Message.error({
                content: e.message || '文件上传失败'
            })
        }
    }
}
export default putFile
