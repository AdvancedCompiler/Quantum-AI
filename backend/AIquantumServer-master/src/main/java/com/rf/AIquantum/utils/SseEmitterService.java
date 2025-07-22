package com.rf.AIquantum.utils;

/**
 * @Author:zzf
 * @Date:2025/3/10:18:25
 * @Description:
 */
import com.alibaba.fastjson.JSONObject;
import com.rf.AIquantum.dao.dto.SseResultDataDto;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void addEmitter(String clientId, SseEmitter emitter) {
        emitters.put(clientId, emitter);
    }

    public void removeEmitter(String clientId) {
        emitters.remove(clientId);
    }

    public SseEmitter getEmitter(String clientId) {
        return emitters.get(clientId);
    }

    public void sendMessage(String clientId, SseResultDataDto message) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(message);
            } catch (Exception e) {
                // 处理异常，如网络问题等
                emitters.remove(clientId);
            }
        }
    }

    public void stopEmitter(String clientId) {
        SseEmitter emitter = emitters.get(clientId);
        if(emitter != null) {
            emitter.complete();
        }

    }
}
