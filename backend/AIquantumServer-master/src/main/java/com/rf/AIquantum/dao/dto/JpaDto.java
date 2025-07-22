package com.rf.AIquantum.dao.dto;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author lpf
 * @description: 自定义注解类，加载dto上表示这是个JpaDto类，解决jpa原生不能返回dto的问题
 * @date 2022/4/6 21:08
 */

@Documented
@Component
@Target(value = {ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JpaDto {
}
