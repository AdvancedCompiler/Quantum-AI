package com.rf.AIquantum.param.service;

import com.rf.AIquantum.param.model.ParamEntry;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface ParamService {
    /**
     * 保存
     * @param paramEntry
     * @return
     */
    ParamEntry save(ParamEntry paramEntry);

    /**
     * 查询，名称
     * @param paramName
     * @return
     */
    ParamEntry findByParamName(String paramName);

    /**
     * 获取到所有
     * @return
     */
    List<ParamEntry> findAll();

    /**
     * 根据类型获取到系统参数
     * @param planContent 类型
     * @return
     */
    List<ParamEntry> findAllByType(String planContent);

    ParamEntry findByParamNameAndType(String roleType, String template);
}
