package com.rf.AIquantum.user.dao.repository;

import com.rf.AIquantum.base.repository.BaseRepository;
import com.rf.AIquantum.user.dao.model.TempUserEntity;

public interface TempUserRepository extends BaseRepository<TempUserEntity,String> {
    TempUserEntity findByIp(String ip);
}
