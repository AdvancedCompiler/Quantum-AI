import { http } from '@/utils/http'

const loginUrl = '/user/login'
const registerUrl = '/user/addUser'
const loginTmpUrl = '/user/temp/login'
export const loginApi = (val: any) => {
    return http<any>(
        {
            method: 'post',
            url: loginUrl,
            data: { ...val }
        })
}
export const registerApi = (val: any) => {
    return http<any>(
        {
            method: 'post',
            url: registerUrl,
            data: { ...val }
        })
}
//账号临时登录
export const loginTmpApi = (val:any) => {
    return http<any>({
        method: 'get',
        url: `${loginTmpUrl}?Ip=${val}`
    })
}