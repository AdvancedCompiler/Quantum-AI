package com.rf.AIquantum.dialogue.rest;

/**
 * @Author:zzf
 * @Date:2025/3/10:17:59
 * @Description:
 */
import com.rf.AIquantum.filter.JwtIgnore;
import com.rf.AIquantum.utils.Result;
import com.rf.AIquantum.utils.SseEmitterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/sse")
@Api(tags = "sse相关接口")
public class SseController {

    private final SseEmitterService sseEmitterService;

    public SseController(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping(value = "/foundSse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation(value = "创建sse连接接口",notes = "参数包括：dialogueId:对话id")
    @JwtIgnore
    public SseEmitter foundSse(String dialogueId) {
        SseEmitter emitter = new SseEmitter(0L); // 不设置超时时间
        sseEmitterService.addEmitter(dialogueId, emitter);

        emitter.onCompletion(() -> sseEmitterService.removeEmitter(dialogueId));
        emitter.onTimeout(() -> sseEmitterService.removeEmitter(dialogueId));

        return emitter;
    }



}