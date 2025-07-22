package com.rf.AIquantum.user.service.impl;

import com.rf.AIquantum.user.dao.model.UserEntity;
import com.rf.AIquantum.user.dao.repository.UserRepository;
import com.rf.AIquantum.user.service.UserService;
import com.rf.AIquantum.utils.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: 用户注册登录等相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Override
    public String getSystemVersion() {
        return Constant.SYSTEM_VERSION;
    }

    @Override
    public UserEntity findUserByPhone(String phone) {
        return this.userRepository.findUserByPhone(phone);
    }

    @Override
    public void save(UserEntity userEntity) {
        this.userRepository.save(userEntity);
    }

    @Override
    public UserEntity findById(String id) {
        return this.userRepository.findById(id).orElse(null);
    }
}
