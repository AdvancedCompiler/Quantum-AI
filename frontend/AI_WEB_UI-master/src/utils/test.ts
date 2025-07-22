

// //获取未读测试记录的条数
// //先判断是否登录
// //获取用户信息
// import { userPlanApi } from '@/api/home'
// import { unreadApi } from '@/api/record'
// import { userInfoStore } from '@/stores'
// const userInfo = userInfoStore()

// //获取已完成的测试但是未读
// export const getUnread = async () => {
//     //判断是否登录
//     if (userInfo.token) {
//         //如果已经登陆了
//         //开始调用
//         //调用接口
//         let res: any = await unreadApi()
//         let num = res.data
//         //将缓存里的数据改变为当前的
//         // userInfo.saveUnreadNum(num)
//     }
// }

// //获取待测试的测试计划
// export const planNumGet = async () => {
//     //如果在就是登录了
//     if (userInfo.token) {
//         let userNo = ''
//         //登录的话
//         //判断当前是否有后台管理

//         userNo = userInfo.userInfo.userNo

//         //调用根据用户查询计划的API
//         // let res: any = await userPlanApi(userNo)
//         // userInfo.savePlanCurrentNum(res.data.length)

//     }
// }