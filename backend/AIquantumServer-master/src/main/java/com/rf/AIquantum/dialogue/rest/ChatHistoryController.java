package com.rf.AIquantum.dialogue.rest;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rf.AIquantum.base.rest.BaseController;
import com.rf.AIquantum.dao.dto.SseResultDataDto;
import com.rf.AIquantum.dialogue.dao.model.ChatHistoryEntity;
import com.rf.AIquantum.dialogue.service.ChatHistoryService;
import com.rf.AIquantum.filter.JwtIgnore;
import com.rf.AIquantum.param.model.ParamEntry;
import com.rf.AIquantum.param.service.ParamService;
import com.rf.AIquantum.utils.Constant;
import com.rf.AIquantum.utils.Result;
import com.rf.AIquantum.utils.SseEmitterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 聊天记录相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
@RestController
@RequestMapping("/chat")
@Slf4j
@Api(tags = "聊天记录相关接口")
public class ChatHistoryController extends BaseController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private SseEmitterService sseEmitterService;
    @Value("${chat_url}")
    private String chat_url;

    @Autowired
    private ParamService paramService;
    @GetMapping("/findChats")
    @ApiOperation(value = "查询聊天记录", notes = "参数包括：pageNum:页码, pageSize:数量, dialogueId:对话id")
    public Result findChatHistorys(@RequestParam int pageNum, @RequestParam int pageSize, @RequestParam String dialogueId) {
        Page<ChatHistoryEntity> chatHistoryEntities = chatHistoryService.findByDialogueIdAndStatus(pageNum, pageSize, dialogueId, 1);
        return success(chatHistoryEntities);
    }

    @PostMapping("/updateChatHistory")
    @ApiOperation(value = "修改聊天记录信息（点赞、点踩）", notes = "ChatHistory对象,点赞时只需将endorse的值改为2；点赞时只需将endorse的值改为3,如果有反馈意见放到feedback字段；endorse：1：未评价（默认）；2：赞同；3：不赞同")
    public Result updateChatHistory(@RequestBody String json) {
        ChatHistoryEntity chatHistoryEntity = JSONObject.parseObject(json, ChatHistoryEntity.class);
        chatHistoryEntity.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        this.chatHistoryService.save(chatHistoryEntity);
        return success("修改成功");
    }

    @PostMapping("/refresh")
    @JwtIgnore
    @ApiOperation(value = "刷新某条回答", notes = "ChatHistory对象")
    public Result refresh(@RequestBody String json) throws InterruptedException {
        ChatHistoryEntity chatHistoryEntity = JSONObject.parseObject(json, ChatHistoryEntity.class);
        String dialogueId = chatHistoryEntity.getDialogueId();
        String createTime = chatHistoryEntity.getCreateTime();
        this.chatHistoryService.deleteByDialogueIdAndCreateTime(dialogueId, createTime);
        //调用模型相关操作
        List<ChatHistoryEntity> chatHistoryEntities = this.chatHistoryService.findChatHistoryByDialogueIdAndStatus(dialogueId);
        JSONArray messages = new JSONArray();
        int length = 0;
        for (int i=chatHistoryEntities.size() -1 ;i>=0;i--) {
            ChatHistoryEntity chatHistory = chatHistoryEntities.get(i);
            JSONArray contents = new JSONArray();
            JSONObject jsonText = new JSONObject();
            jsonText.put("type", "text");
            String text = chatHistory.getContent();
            if(text.contains("</think>")){
                text = text.substring(text.indexOf("</think>")+8);
            }
            if(length + text.length()>Constant.MESSAGE_MAX_LENGTH){
                break;
            }
            length += text.length();
            if (chatHistory.getImage() != null && !chatHistory.getImage().equals("")) {
                if(length+chatHistory.getImage().length()>Constant.MESSAGE_MAX_LENGTH){
                    break;
                }
                JSONObject jsonImage = new JSONObject();
                jsonImage.put("type", "image_url");
                JSONObject jsonUrl = new JSONObject();
                jsonUrl.put("url", chatHistory.getImage());
                length += chatHistory.getImage().length();
                jsonImage.put("image_url", jsonUrl);
                contents.add(jsonImage);
            }
            jsonText.put("text", text);
            contents.add(jsonText);
            JSONObject jsonRole = new JSONObject();
            jsonRole.put("role", chatHistory.getRole());
            jsonRole.put("content", contents);
            messages.add(jsonRole);
        }
        System.out.println("Message length: " + length);
        log.info("Message length: " + length);
        //反转顺序
        int size = messages.size();
        for (int i = 0; i< size; i++){
            Object temp = messages.get(i);
            messages.set(i,messages.get(size-1-i));
            messages.set(size-1-i,temp);
        }
        JSONObject jsonChat = new JSONObject();
        jsonChat.put("messages", messages);
        jsonChat.put("stream",true);

//        String url = Constant.INVOKE_IP_PROT + Constant.CHAT_PATH;
//        String data = HttpClientChat(jsonChat,url);
//        JSONObject jsonSystem = JSONObject.parseObject(data);
//        if (jsonSystem == null || !jsonSystem.containsKey("response")) {
//            return fail("", "模型服务内部错误");
//        }
//        String content = jsonSystem.getString("response");
        SseResultDataDto sseResultDataDto = new SseResultDataDto();
        sseResultDataDto.setDialogueId(dialogueId);
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
        StringBuilder stringBuilder = new StringBuilder();
        // 创建请求体
        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(jsonChat.toJSONString(), MediaType.parse("application/json"));
        ParamEntry paramEntry = this.paramService.findByParamName("chat_url");
        if (paramEntry != null) {
            chat_url = paramEntry.getParamValue();
        }
        // 创建请求
        Request request = new Request.Builder().header("Content-Type", "application/json").header("Accept", "text/event-stream")
                .url(chat_url + Constant.CHAT_PATH)
                .post(requestBody)
                .build();
        // 发送请求并处理响应
        String progress = "undo";
        try (Response response = client.newCall(request).execute()) {
            progress = "doing";
            if (!response.isSuccessful()) {
                log.error("Unexpected code " + response);
                return fail();
            }
            // 获取响应流
            okhttp3.ResponseBody responseBody = response.body();
            String flag = "think";
            if (responseBody != null) {
                BufferedSource bufferedSource = responseBody.source();
                try {
                    byte[] buffer = new byte[1024];
                    while (!bufferedSource.exhausted()) {
                        int bytesRead = bufferedSource.read(buffer);
                        String datas = new String(buffer, 0, bytesRead);
                        if (sseEmitterService.getEmitter(dialogueId) == null) {
                            if (flag.equals("think")) {
                                stringBuilder.append("</").append(flag).append(">");
                            }
                            break;
                        }
                        log.info("datas==" + datas+"===end");
                        String[] lines  = datas.split("<quan-cut>");
                        for (String line : lines) {

                            sseResultDataDto.setType(flag);
                            System.out.println("line==" + line);
                            log.info("line==="+line+"===");
                            JSONObject jsonObject = null;
                            try {
                                if (StringUtils.isNotEmpty(line)) {
                                    jsonObject = JSONObject.parseObject(line);
                                }

                            } catch (Exception e) {
                                log.error(line + "json解析失败", e);
                                continue;

                            }
                            String event = jsonObject.getString("event");
                            if (event.equals("error")) {
                                if (flag.equals("think")) {
                                    stringBuilder.append("</").append(flag).append(">");
                                }
                                break;
                            }
                            String data = jsonObject.getString("data");
                            if (StringUtils.isNotEmpty(data)) {
                                stringBuilder.append(data);
                                if (data.contains("</think>")) {
                                    flag = "text";
                                    data = data.replace("</think>", "");
                                }
                                if (data.contains("<think>")) {
                                    data = data.replace("<think>", "");
                                }
                                sseResultDataDto.setContent(data);
                                log.info("发送消息:{}", sseResultDataDto);
                                sseEmitterService.sendMessage(dialogueId, sseResultDataDto);
                            }
                            Thread.sleep(5);
                        }

                    }
                    progress = "done";
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    bufferedSource.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return fail();
        }
        chatHistoryEntity = new ChatHistoryEntity();
        chatHistoryEntity.setDialogueId(dialogueId);
        chatHistoryEntity.setRole("system");
        chatHistoryEntity.setContent(stringBuilder.toString());
        chatHistoryEntity.setStatus(1);
        chatHistoryEntity.setEndorse(1);
        chatHistoryEntity.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        chatHistoryEntity.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        chatHistoryEntity = this.chatHistoryService.save(chatHistoryEntity);
        while (progress.equals("doing")) {
            Thread.sleep(100);
        }
        return success(chatHistoryEntity);
    }

    @GetMapping("/clearChats")
    @ApiOperation(value = "清空聊天记录", notes = "参数包括：dialogueId:对话id")
    public Result clearChats(@RequestParam String dialogueId) {
        this.chatHistoryService.deleteByDialogueId(dialogueId);
        return success();
    }

    @PostMapping("/exportPdf")
    @ApiOperation(value = "导出PDF")
    public String exportPdf(@RequestBody String json, HttpServletResponse response) {
        List<ChatHistoryEntity> chatHistoryEntityList = JSONArray.parseArray(json, ChatHistoryEntity.class);
        if (chatHistoryEntityList.size() > 0) {
            return "导出内容为空";
            /*UserRecordEntity userRecordEntity = this.userRecordService.getUserRecordById(id);
            if (userRecordEntity == null) {
                return "该记录异常，下载失败";
            }
            String PDFPath = generatePDFRecord(userRecordEntity, "pdf");
            if (PDFPath.equals("false")) {
                return "报告版本较早，暂无法下载";
            }
//            List<String> htmlFilePathList = new ArrayList<>();
//            htmlFilePathList.add(PDFPath);
//
//            //生成PDF测试记录
//            try {
//                html2pdf(htmlFilePathList);
//            } catch (Exception e) {
//                log.error("生成测试文件失败" + e.getMessage());
//                e.printStackTrace();
//                return "下载失败";
//            }
//            String[] split = userRecordEntity.getFileName().split("/");
//            String fileName = split[split.length - 1];
//            String PDFName = fileName.replace(".xlsx", ".pdf");
            //下载PDF文件
            File z = new File("./h2p/export_pdf/PDFReport/" + PDFPath);
            if (!z.exists()) return "下载失败";
            try {
                FileUtil.downloadFile(response, z, PDFPath, true);
                return "下载成功";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return "下载失败";
            }*/
        } else {
            return "导出内容为空";
        }
    }

}
