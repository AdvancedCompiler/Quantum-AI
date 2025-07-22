package com.rf.AIquantum.user.service.impl;

import com.rf.AIquantum.user.dao.model.TempUserEntity;
import com.rf.AIquantum.user.dao.repository.TempUserRepository;
import com.rf.AIquantum.user.service.TempUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author:zzf
 * @Date:2025/3/21:16:32
 * @Description:
 */
@Service
public class TempUserServiceImpl implements TempUserService {
    @Autowired
    private TempUserRepository tempUserRepository;
    @Override
    public TempUserEntity findByIp(String ip) {
        return this.tempUserRepository.findByIp(ip);
    }

    @Override
    public TempUserEntity save(TempUserEntity tempUserEntity) {
        return this.tempUserRepository.save(tempUserEntity);
    }
}
