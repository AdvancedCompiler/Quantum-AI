//引入axios
import axios from "axios";

//引入用户信息组件
import { userInfoStore } from "@/stores";

import { ElMessage, ElLoading } from "element-plus";

// import { useRouter } from 'vue-router';
import router from "@/router";

const userInfo = userInfoStore();

// export const base_url = 'http://10.113.248.3:8062'
// export const base_url = 'http://10.113.248.3:8062'
// export const base_url = import.meta.env.VITE_BASE_API
//双洋本地
// export const base_url = 'http://10.113.248.5:8062'
// export const base_url = "http://49.232.26.44:8062";
export const base_url = import.meta.env.VITE_API_BASE_URL

//设置axios 默认访问路径
axios.defaults.baseURL = base_url;
//设置超时时间
axios.defaults.timeout = 300000000;

// 设置头部信息
axios.defaults.headers.common["Content-Type"] =
  "application/JSON;charset=UTF-8";

//前端跨域配置
axios.defaults.withCredentials = true;

//设置 使用token的白名单
const whiteList = [];

//设置请求前的拦截器
axios.interceptors.request.use(
  (config) => {
    //在请求前需要做什么
    //如果有token---则放在请求头上
    config.headers.Authorization = userInfo.token
      ? `Bearer ${userInfo.token}`
      : "";
    return config;
  },
  (error) => {
    //对错误请求需要做些什么
    return Promise.reject(error);
  }
);

//添加相应拦截器
const loadingWhiteList=['/chat/updateChatHistory','/chat/refresh']
axios.interceptors.response.use(
  (response:any) => {
    
    if(!loadingWhiteList.includes(response.config.url)){
        // ElLoading.service().close();
    }
    //对响应中做些什么
    if (response.data.code === 401) {
      // 重定向到登录页
      //登录状态失效

      //取到缓存里的登录信息
      //---将token 去掉
      //出现401 浏览器中还存在缓存   就先提示过期了
      //然后清除用户信息缓存--重新登录
      if (userInfo.token) {
        ElMessage({
          message: response.data.msg,
          type: "warning",
        });
      
        // menuStatus.saveActiveIndex('6')
      }
      router.push({ path: "/" });
      //清除用户缓存
      userInfo.clearUserInfo();
      userInfo.clearActiveIndex()
      userInfo.saveDialogueId("");
      userInfo.clearPreChat()
      // router.push({ name: 'login' })
      //此时报告找不到路径
      // Toast.fail(response.data.msg);
      // Message.error(response.data.msg);
    }
    return response;
  },
  (error) => {
    Promise.reject(error);
  }
);

//封装请求
export const http = <T>(option: any) => {
  return new Promise((resolve, reject) => {
    axios({
      ...option,
    })
      .then((res) => {
        //在这里应该机解密下
        resolve(res.data);
      })
      .catch((error) => {
        reject(error);
      });
  });
};
