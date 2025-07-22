package com.rf.AIquantum.user.service;

import com.rf.AIquantum.user.dao.model.UserEntity;

/**
 * @Description: 用户注册登录等相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
public interface UserService {

    public String getSystemVersion();

    UserEntity findUserByPhone(String phone);

    void save(UserEntity userEntity);

    UserEntity findById(String id);
}
