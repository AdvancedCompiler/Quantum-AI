package com.rf.AIquantum.dialogue.rest;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rf.AIquantum.base.rest.BaseController;
import com.rf.AIquantum.dao.dto.SseResultDataDto;
import com.rf.AIquantum.dialogue.dao.model.ChatHistoryEntity;
import com.rf.AIquantum.dialogue.dao.model.DialogueEntity;
import com.rf.AIquantum.dialogue.service.ChatHistoryService;
import com.rf.AIquantum.dialogue.service.DialogueService;
import com.rf.AIquantum.filter.JwtIgnore;
import com.rf.AIquantum.param.model.ParamEntry;
import com.rf.AIquantum.param.service.ParamService;
import com.rf.AIquantum.user.dao.model.TempUserEntity;
import com.rf.AIquantum.user.dao.model.UserEntity;
import com.rf.AIquantum.user.service.TempUserService;
import com.rf.AIquantum.user.service.UserService;
import com.rf.AIquantum.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 对话相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
@RestController
@RequestMapping("/dialogue")
@Slf4j
@Api(tags = "对话相关接口")
public class DialogueController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private DialogueService dialogueService;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private SseEmitterService sseEmitterService;

    @Autowired
    private TempUserService tempUserService;

    @Value("${chat_url}")
    private String chat_url;

    @Autowired
    private ParamService paramService;

    @PostMapping("/saveChat")
    @JwtIgnore
    @ApiOperation(value = "保存对话", notes = "参数包括：phone:手机号, dialogueId:对话id（为空时是新建对话）, content:消息内容,image：图片（可以为空）")
    public Result saveChat(MultipartFile image, String phone, String dialogueId, String content) throws InterruptedException {
        TempUserEntity tempUser = this.tempUserService.findByIp(phone);
        if (tempUser != null) {
            if (tempUser.getRemainingCount() <= 0) {
                return noAuth("体验次数已用完，请注册新账号！");
            } else {
                tempUser.setRemainingCount(tempUser.getRemainingCount() - 1);
                this.tempUserService.save(tempUser);
            }
        } else {
            UserEntity user = this.userService.findUserByPhone(phone);
            if (user == null) {
                return fail(null, "用户不存在");
            }
        }


        String imageUrl = "";
        if (image != null) {
            if (!image.isEmpty()) {
                String FILEDIR = "./uploadFile/";
                String fileName = phone + "-=-" + System.currentTimeMillis() + "-=-" + image.getOriginalFilename();
                try {
                    File temp = new File(FILEDIR);
                    if (!temp.exists()) {
                        temp.mkdirs();
                    }
                    File fileLocal = new File(FILEDIR, fileName);
                    FileUtils.copyInputStreamToFile(image.getInputStream(), fileLocal);
                    imageUrl = Constant.CHAT_IMAGE_URL + FILEDIR + fileName;
                } catch (Exception e) {
                    e.printStackTrace();
                    return fail("文件上传失败");
                }
            }
        }
        List<ChatHistoryEntity> chatHistoryEntities = chatHistoryService.findChatHistoryByDialogueIdAndStatus(dialogueId);
        if (chatHistoryEntities.size() < 1) {

            //新建对话
            DialogueEntity dialogueEntity = new DialogueEntity();
            dialogueEntity.setId(dialogueId);
            if (content.length() > 50) {
                dialogueEntity.setDialogueName(content.substring(0, 50));
            } else {
                dialogueEntity.setDialogueName(content);
            }
            dialogueEntity.setPhone(phone);
            dialogueEntity.setStatus(1);
            dialogueEntity.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
            dialogueEntity.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
            this.dialogueService.insert(dialogueEntity);
        }
        ChatHistoryEntity chatHistoryEntity = new ChatHistoryEntity();
        chatHistoryEntity.setDialogueId(dialogueId);
        chatHistoryEntity.setRole("user");
        chatHistoryEntity.setContent(content);
        chatHistoryEntity.setImage(imageUrl);
        chatHistoryEntity.setStatus(1);
        chatHistoryEntity.setEndorse(1);
        chatHistoryEntity.setCreateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        chatHistoryEntity.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        this.chatHistoryService.save(chatHistoryEntity);
        //调用模型相关操作
        chatHistoryEntities = this.chatHistoryService.findChatHistoryByDialogueIdAndStatus(dialogueId);
        JSONArray messages = new JSONArray();
        int length = 0;
        for (int i = chatHistoryEntities.size() - 1; i >= 0; i--) {
            ChatHistoryEntity chatHistory = chatHistoryEntities.get(i);
            JSONArray contents = new JSONArray();
            JSONObject jsonText = new JSONObject();
            jsonText.put("type", "text");
            String text = chatHistory.getContent();
            if (text.contains("</think>")) {
                text = text.substring(text.indexOf("</think>") + 8);
            }
            if (length + text.length() > Constant.MESSAGE_MAX_LENGTH) {
                break;
            }
            length += text.length();
            if (StringUtils.isNotEmpty(chatHistory.getImage())) {
                if (length + chatHistory.getImage().length() > Constant.MESSAGE_MAX_LENGTH) {
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
        System.out.println("Message length =" + length);
        log.info("Message length =" + length);
        //反转顺序
        int size = messages.size();
        for (int i = 0; i < size; i++) {
            Object temp = messages.get(i);
            messages.set(i, messages.get(size - 1 - i));
            messages.set(size - 1 - i, temp);
        }
        JSONObject jsonChat = new JSONObject();
        jsonChat.put("messages", messages);
        jsonChat.put("stream", true);

       /* String url = Constant.INVOKE_IP_PROT + Constant.CHAT_PATH;
        String data = HttpClientChat(jsonChat,url);
        JSONObject jsonSystem = JSONObject.parseObject(data);
        if (jsonSystem == null || !jsonSystem.containsKey("response")) {
            return fail("", "模型服务内部错误");
        }
        content = jsonSystem.getString("response");*/
        SseResultDataDto sseResultDataDto = new SseResultDataDto();
        sseResultDataDto.setDialogueId(dialogueId);
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.MINUTES).writeTimeout(60, TimeUnit.MINUTES).readTimeout(60, TimeUnit.MINUTES).build();
        StringBuilder stringBuilder = new StringBuilder();
        log.info("payload=" + jsonChat.toJSONString());
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

    @GetMapping("/stop/{dialogueId}")
    @ApiOperation(value = "停止对话", notes = "参数包括：dialogueId:对话id")
    public Result stop(@PathVariable String dialogueId) {

        sseEmitterService.stopEmitter(dialogueId);
        return success();
    }

    @GetMapping("/findDialogues")
    @ApiOperation(value = "查询对话列表", notes = "参数包括：pageNum:页码, pageSize:数量, search:搜索内容")
    public Result findDialogues(@RequestParam int pageNum, @RequestParam int pageSize, @RequestParam String search, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String[] tokens = token.split(" ");
        if (tokens.length != 2) {
            token = tokens[0];
        } else {
            token = tokens[1];
        }
        JWTUtil.verify(token);
        DecodedJWT verify = JWTUtil.verify(token);
        String phone = verify.getClaim("phone").asString();
        Page<DialogueEntity> dialogueEntities = dialogueService.findByPhoneAndStatusAndSearch(pageNum, pageSize, phone, 1, search);
        return success(dialogueEntities);
    }

    @PostMapping("/updateDialogues")
    @ApiOperation(value = "修改对话信息（重命名和删除）", notes = "Dialogues对象,修改对话名称时只需将修改后的名称放入dialogueName中；删除对话名称时只需将status的值改为0")
    public Result updateDialogues(@RequestBody String json) {
        DialogueEntity dialogueEntity = JSONObject.parseObject(json, DialogueEntity.class);
        dialogueEntity.setUpdateTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"));
        this.dialogueService.save(dialogueEntity);
        return success("修改成功");
    }
    /*@GetMapping("/sendMsg")
    @ApiOperation(value = "sse测试接口",notes = "参数包括：dialogueId:对话id")
    @JwtIgnore
    public Result sendMsg(@RequestParam String dialogueId) throws UnsupportedEncodingException {
        List<ChatHistoryEntity> chatHistoryEntities = this.chatHistoryService.findChatHistoryByDialogueIdAndStatus(dialogueId);
        JSONArray messages = new JSONArray();
        for (ChatHistoryEntity chatHistory : chatHistoryEntities) {
            JSONArray contents = new JSONArray();
            JSONObject jsonText = new JSONObject();
            jsonText.put("type","text");
            jsonText.put("text",chatHistory.getContent());
            if (chatHistory.getImage() != null && !chatHistory.getImage().equals("")) {
                JSONObject jsonImage = new JSONObject();
                jsonImage.put("type","image_url");
                JSONObject jsonUrl = new JSONObject();
                jsonUrl.put("url",chatHistory.getImage());
                jsonImage.put("image_url",jsonUrl);
                contents.add(jsonImage);
            }
            contents.add(jsonText);
            JSONObject jsonRole = new JSONObject();
            jsonRole.put("role",chatHistory.getRole());
            jsonRole.put("content",contents);
            messages.add(jsonRole);
        }
        JSONObject jsonChat = new JSONObject();
        jsonChat.put("messages",messages);
        jsonChat.put("stream",true);

        String url = Constant.INVOKE_IP_PROT + Constant.CHAT_PATH;
        String data = HttpClientChat(jsonChat,url);
        JSONObject jsonSystem = JSONObject.parseObject(data);
        if (jsonSystem == null || !jsonSystem.containsKey("response")) {
            return fail("", "模型服务内部错误");
        }
        String content = jsonSystem.getString("response");
        return success();
    }*/

    public static String HttpClientChat(JSONObject jsonChat, String url) throws UnsupportedEncodingException {
        System.out.println("11:" + jsonChat);
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(jsonChat.toString(), "UTF-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        HttpResponse response = null;
        String responseData = null;
        try {
            response = httpclient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                responseData = EntityUtils.toString(response.getEntity());
                System.out.println("22:" + responseData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return responseData;
    }


}
