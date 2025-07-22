package com.rf.AIquantum.filter;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rf.AIquantum.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @author lpf
 * @description:
 * @date 2021/01/20 16:54
 */
public class UserCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HashMap<String, Object> resultJson = new HashMap<>();
        String uri = request.getRequestURI();
        String token = request.getHeader("Authorization");
        token = token.split(" ")[1];
        JWTUtil.verify(token);
        DecodedJWT verify = JWTUtil.verify(token);
        String userId = verify.getClaim("userId").asString();
        String type = verify.getClaim("type").asString();
        return true;
    }
}
