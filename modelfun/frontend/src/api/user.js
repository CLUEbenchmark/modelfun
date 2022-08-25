import service from '@/utils/request'
/**
 * 根据用户名和密码登录 data: { username, password }
 * @param {String} username
 * @param {String} password
 */
export function login(data) {
    return service({
        url: '/doLogin',
        method: 'post',
        data
    })
}