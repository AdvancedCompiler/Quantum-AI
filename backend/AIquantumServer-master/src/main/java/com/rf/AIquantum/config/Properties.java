package com.rf.AIquantum.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:zzf
 * @Date:2022/7/5:18:15
 * @Description:
 */

public class Properties {
    private final Map<String, Duration> initCaches = new HashMap<>();
    public Map<String ,Duration> getInitCache(){

        return initCaches;
    }
}
