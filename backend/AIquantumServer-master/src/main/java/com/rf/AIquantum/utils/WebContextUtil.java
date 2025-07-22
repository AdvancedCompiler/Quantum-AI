package com.rf.AIquantum.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description:线程缓存工具类
 * @Author: mimang
 * @Date: 2024/9/6
 */
public class WebContextUtil {
    //本地线程缓存token
    private static ThreadLocal<String> local = new ThreadLocal<>();

    /**
     * 设置token信息
     * @param content
     */
    public static void setUserToken(String content){
        removeUserToken();
        local.set(content);
    }

    /**
     * 获取token信息
     * @return
     */
    /*public static UserEntity getUserToken(){
        if(local.get() != null){
            UserEntity userToken = JSONObject.parseObject(local.get() , UserEntity.class);
            return userToken;
        }
        return null;
    }*/

    /**
     * 移除token信息
     * @return
     */
    public static void removeUserToken(){
        if(local.get() != null){
            local.remove();
        }
    }
}
