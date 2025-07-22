package com.rf.AIquantum.param.service.impl;

import com.rf.AIquantum.param.model.ParamEntry;
import com.rf.AIquantum.param.repository.ParamRepository;
import com.rf.AIquantum.param.service.ParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author:zzf
 * @Date:2022/7/5:10:53
 * @Description:系统参数
 */
@Service
public class ParamServiceImpl implements ParamService {

    @Autowired
    private ParamRepository repository;

    //@Autowired
    //private RedisTemplate<String,Object> template;
    /**
     * 保存
     *
     * @param paramEntry
     * @return
     */
    @Override
    @CachePut(value = "param", key = "#paramEntry.paramName")
    public ParamEntry save(ParamEntry paramEntry) {
        return  this.repository.save(paramEntry);
    }

    /**
     * 查询，名称
     *
     * @param paramName
     * @return
     */
    @Override
    public ParamEntry findByParamName(String paramName) {
        return this.repository.findByParamName(paramName);
    }

    @Override
    public List<ParamEntry> findAll() {
        return this.repository.findAll();
    }

    /**
     * 根据类型获取系统参数
     * @param planContent 类型
     * @return
     */
    @Override
    public List<ParamEntry> findAllByType(String planContent) {
        return repository.findAllByParamType(planContent);
    }

    @Override
    public ParamEntry findByParamNameAndType(String roleType, String template) {
        return repository.findByParamNameAndParamType(roleType,template);
    }


}
