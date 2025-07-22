package com.rf.AIquantum.param.repository;

import com.rf.AIquantum.base.repository.BaseRepository;
import com.rf.AIquantum.param.model.ParamEntry;

import java.util.List;

public interface ParamRepository extends BaseRepository<ParamEntry,String> {


    ParamEntry findByParamName(String paramName);

    List<ParamEntry> findAllByParamType(String planContent);

    ParamEntry findByParamNameAndParamType(String roleType, String template);
}
