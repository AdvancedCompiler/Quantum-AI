package com.rf.AIquantum.user.dao.repository;

import com.rf.AIquantum.base.repository.BaseRepository;
import com.rf.AIquantum.user.dao.model.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @Description: 用户注册登录等相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
public interface UserRepository extends BaseRepository<UserEntity, String> {


    UserEntity findUserByPhone(String phone);
}
