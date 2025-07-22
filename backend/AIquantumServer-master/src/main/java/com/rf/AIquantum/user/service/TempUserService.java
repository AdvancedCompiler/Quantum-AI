package com.rf.AIquantum.user.service;

import com.rf.AIquantum.user.dao.model.TempUserEntity;

public interface TempUserService {
    TempUserEntity findByIp(String ip);

    TempUserEntity save(TempUserEntity tempUserEntity);
}
