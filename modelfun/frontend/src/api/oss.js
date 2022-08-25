import service from '@/utils/request'
export const getSTSToken = () => {
    return service({
        url:  `/oss/sts`,
        method: 'get',
    })
}
//获取文件地址
export const getFileUrl = (data) => {
    return service({
        url:  `/oss/get_file_url?filePath=`+data.filePath,
        method: 'get',
    })
}