package com.rf.AIquantum.user.rest;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.rf.AIquantum.base.rest.BaseController;
import com.rf.AIquantum.user.dao.model.TempUserEntity;
import com.rf.AIquantum.user.dao.model.UserEntity;
import com.rf.AIquantum.user.service.TempUserService;
import com.rf.AIquantum.user.service.UserService;
import com.rf.AIquantum.utils.Constant;
import com.rf.AIquantum.utils.JWTUtil;
import com.rf.AIquantum.utils.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.parser.Token;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * @Description: 用户注册登录等相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户注册登录等相关接口")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private TempUserService tempUserService;

    @PostMapping("/addUser")
    @ApiOperation(value = "新增用户",notes = "参数包括：userName:用户名, phone：手机号, password:密码(MD5加密后的字符串)")
    public Result addOrUpdateUser(@RequestBody String jsonParam){
        JSONObject jsonObject =JSONObject.parseObject(jsonParam);
        if (!jsonObject.containsKey("phone") || !jsonObject.containsKey("password") || !jsonObject.containsKey("userName")) {
            return fail("", "手机号、密码、用户名不能为空");
        }
        String phone = jsonObject.getString("phone");
        UserEntity user = this.userService.findUserByPhone(phone);
        if (user != null){
            return fail(null,"手机号已存在");
        }
        user = new UserEntity();
        user.setPhone(phone);
        user.setPassword(jsonObject.getString("password"));
        user.setUserName(jsonObject.getString("userName"));
        user.setRoleType("1");
        user.setCreateTime(DateUtil.now());
        user.setUpdateTime(DateUtil.now());
        userService.save(user);
        return success("操作成功");
    }

    @ApiOperation(value = "系统登录接口",notes = "参数包括：phone:账号, password:密码(MD5加密后的字符串)")
    @PostMapping("/login")
    public Result login(HttpServletRequest request, @RequestBody String jsonParam){
        try {
            JSONObject jsonObject =JSONObject.parseObject(jsonParam);
            if (!jsonObject.containsKey("phone") || !jsonObject.containsKey("password") ) {
                return fail("", "账号或密码不能为空");
            }
            String password = jsonObject.getString("password");
            String phone = jsonObject.getString("phone");
            UserEntity userEntity = this.userService.findUserByPhone(phone);
            if (userEntity == null) {
                return fail(null, "用户不存在");
            }
            if (userEntity.getPassword().equals(password)) {
                userEntity.setPassword(null);
                String token = JWTUtil.getTokenByUserInfo(userEntity);
                JSONObject resultJson = new JSONObject();
                resultJson.put("user", userEntity);
                resultJson.put("token", token);
                request.getSession().setAttribute("user", userEntity);
                return success(resultJson);
            } else {
                return fail(null, "密码错误");
            }
        }catch (Exception e){
            e.printStackTrace();
            return fail();
        }
    }

    /**
     * 修改密码
     * @param jsonParam 待修改用户密码参数
     * @return
     */
    @ApiOperation(value = "修改密码",notes = "data参数包括：id:用户id,password:新密码(MD5加密后的字符串),oldPassword:旧密码(MD5加密后的字符串)")
    @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody String jsonParam){
        try {
            JSONObject jsonObject =JSONObject.parseObject(jsonParam);
            if(!jsonObject.containsKey("id")|| StringUtils.isEmpty(jsonObject.getString("id")))
                return failBadRequest(null,"用户id不能为空！");
            if(!jsonObject.containsKey("oldPassword")|| StringUtils.isEmpty(jsonObject.getString("oldPassword")))
                return failBadRequest(null,"旧密码不能为空！");
            if(!jsonObject.containsKey("password")|| StringUtils.isEmpty(jsonObject.getString("password")))
                return failBadRequest(null,"新密码不能为空！");
            String password =  jsonObject.getString("password");
            String oldPassword = jsonObject.getString("oldPassword");
            String id = jsonObject.getString("id");
            UserEntity userEntity = userService.findById(id);
            if (userEntity == null) {
                return fail(null, "用户不存在");
            }
            if (!userEntity.getPassword().equals(oldPassword)) {
                return fail("", "旧密码有误");
            }
            userEntity.setPassword(password);
            userEntity.setUpdateTime(DateUtil.now());
            this.userService.save(userEntity);
            return success();
        }catch (Exception e){
            e.printStackTrace();
            return fail();
        }
    }

    /**
     * 修改用户名
     * @param
     * @return
     */
    @ApiOperation(value = "修改用户名",notes = "data参数包括：id:用户id,userName:新用户名")
    @PostMapping("/updateUserName")
    public Result updateUserName(@RequestBody String jsonParam){
        JSONObject jsonObject =JSONObject.parseObject(jsonParam);
        if(!jsonObject.containsKey("id")|| StringUtils.isEmpty(jsonObject.getString("id")))
            return failBadRequest(null,"用户id不能为空！");
        if(!jsonObject.containsKey("userName")|| StringUtils.isEmpty(jsonObject.getString("userName")))
            return failBadRequest(null,"旧密码不能为空！");
        String id = jsonObject.getString("id");
        String userName = jsonObject.getString("userName");
        if (userName == null || userName.equals("")){
            return fail(null, "用户名不能为空");
        }
        if (userName.length() > 50){
            return fail(null, "用户名过长");
        }
        UserEntity userEntity = userService.findById(id);
        if (userEntity == null) {
            return fail(null, "用户不存在");
        }
        userEntity.setUserName(userName);
        userEntity.setUpdateTime(DateUtil.now());
        this.userService.save(userEntity);
        return success();
    }

    /**
     * 修改用户头像
     * @param
     * @return
     */
    @ApiOperation(value = "修改用户头像",notes = "data参数包括：id:用户id,avatar:用户头像文件")
    @PostMapping("/updateAvatar")
    public Result updateAvatar(String id, MultipartFile avatar){
        if (avatar == null) {
            return fail("头像为空");
        } else {
            UserEntity userEntity = userService.findById(id);
            if (userEntity == null) {
                return fail(null, "用户不存在");
            }
            String fileName = "";
            String FILEDIR = "./userAvatar/";
            String avatarUrl = "";
            if (!avatar.isEmpty()) {
                fileName = userEntity.getPhone() + "-=-" + System.currentTimeMillis() + "-=-" + avatar.getOriginalFilename();
                try {
                    File temp = new File(FILEDIR);
                    if (!temp.exists()) {
                        temp.mkdirs();
                    }
                    File fileLocal = new File(FILEDIR, fileName);
                    FileUtils.copyInputStreamToFile(avatar.getInputStream(), fileLocal);
                    avatarUrl = FILEDIR + fileName;
                    userEntity.setAvatar(avatarUrl);
                    userEntity.setUpdateTime(DateUtil.now());
                    this.userService.save(userEntity);
                    return success(userEntity.getAvatar());
                } catch (Exception e) {
                    e.printStackTrace();
                    return fail("文件上传失败");
                }
            } else {
                return fail("图片为空");
            }
        }
    }

    @GetMapping(value = "/show",produces = MediaType.IMAGE_JPEG_VALUE )
    @ResponseBody
    @ApiOperation(value = "图片展示",notes = "filePath：相对路径")
    public byte[] showPhoto(String filePath) throws Exception {
        try{
            File file = new File(filePath);
            if(file.exists()){
                FileInputStream inputStream = new FileInputStream(file);
                byte [] bytes = new byte[inputStream.available()];
                inputStream.read(bytes,0,inputStream.available());
                return bytes;
            }
            return null;
        }catch(Exception e){
            throw new Exception("未找到图片");
        }
    }
    /**
     * 登出
     * @param userNo 账号
     * @desc
     * @return
     */
    /*@ApiOperation(value = "登出",notes = "json字符串形式传参（加密）,data参数包括:userNo:账号" )
    @PostMapping("/logout")
    public Result logout(@RequestParam String userNo){
        try {
            if ( StringUtils.isEmpty( userNo)    ) {
                return fail("", "账号不能为空");
            }
            //首先校验用户是否存在
            UserEntity userEntity = userService.findUserByUserNo(userNo);
            if (userEntity == null){
                return fail(null,"用户不存在");
            }
            return success();
        }catch (Exception e){
            e.printStackTrace();
            return fail();
        }
    }*/


    /**
     * 临时登录
     */
    @ApiOperation(value = "临时登录",notes = "json字符串形式传参（加密）,data参数包括:userNo:账号" )
    @GetMapping("/temp/login")
    public Result login(@RequestParam String Ip){
        TempUserEntity tempUserEntity = this.tempUserService.findByIp(Ip);
        if(tempUserEntity == null){
            tempUserEntity = new TempUserEntity();
            tempUserEntity.setIp(Ip);
            tempUserEntity.setRemainingCount(2);
            tempUserEntity.setUsername("测试用户");
            tempUserEntity = this.tempUserService.save(tempUserEntity);
        }else {
            if(tempUserEntity.getRemainingCount() == 0){
                return noAuth("体验次数已用完，请注册新账号！");
            }
        }
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName("临时用户");
        userEntity.setPhone(Ip);
        String token = JWTUtil.getTokenByUserInfo(userEntity);
        JSONObject resultJson = new JSONObject();
        resultJson.put("user", userEntity);
        resultJson.put("token", token);
        resultJson.put("remainingCount", tempUserEntity.getRemainingCount());
        return success(resultJson);
    }

}
