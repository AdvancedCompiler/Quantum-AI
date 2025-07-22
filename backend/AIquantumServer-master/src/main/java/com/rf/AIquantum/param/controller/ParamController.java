package com.rf.AIquantum.param.controller;

import com.rf.AIquantum.base.rest.BaseController;
import com.rf.AIquantum.filter.JwtIgnore;
import com.rf.AIquantum.param.model.ParamEntry;
import com.rf.AIquantum.param.service.ParamService;
import com.rf.AIquantum.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author:zzf
 * @Date:2022/7/1:18:12
 * @Description:系统参数控制器
 */
@Slf4j
@RestController
@RequestMapping("/param")
@Api(tags = "系统参数管理")
@Profile("test")
public class ParamController extends BaseController {

    @Autowired
    private ParamService paramService;

    @PostMapping("/save")
    public Result save(@RequestBody ParamEntry param )  {
        try {
            paramService.save(param);
            return success(param,"操作成功");
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return fail(null,"操作失败");
        }
    }

    @GetMapping("/find/{paramName}")
    public Result find(@PathVariable String paramName){
        try {
            ParamEntry paramEntry = this.paramService.findByParamName(paramName);
            return success(paramEntry);
        }catch (Exception e){
            e.printStackTrace();
            log.error("{}------"+e.getMessage(),ParamController.class.getName());
            return fail(null,"操作失败");
        }
    }


    @GetMapping("/findAllByType")
    @ApiOperation(value = "根据类型查找")
    public Result findAllByType(@RequestParam String type ){
        try {
            List<ParamEntry> paramEntries = paramService.findAllByType(type);
            return success(paramEntries,"操作成功");
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            return fail(null,"操作失败");
        }
    }

    @GetMapping("/findAll")
    public Result findAll(){
       try {
           List<ParamEntry> list = paramService.findAll();
           return success(list);
       }catch (Exception e){
           e.printStackTrace();
           log.error(e.getMessage());
           return fail();
       }
    }

    @PostMapping("/update")
    @JwtIgnore
    public Result updateValue(@RequestBody ParamEntry paramEntry){
            ParamEntry entry = paramService.findByParamName(paramEntry.getParamName());
            if (entry == null){
                this.paramService.save(paramEntry);
            }else{
                entry.setParamValue(paramEntry.getParamValue());
                paramService.save(entry);
            }

            return success(entry);
    }
}
