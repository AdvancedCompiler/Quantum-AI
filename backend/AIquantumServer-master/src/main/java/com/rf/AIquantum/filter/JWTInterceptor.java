package com.rf.AIquantum.filter;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.rf.AIquantum.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * @author lpf
 * @description:
 * @date 2021/12/2821:48
 */
@Slf4j
public class JWTInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HashMap<String, Object> resultJson = new HashMap<>();
        String uri = request.getRequestURI();
        try {
            String token = request.getHeader("Authorization");
            if(StringUtils.isEmpty(token)) {
                resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
                resultJson.put("msg", "无效签名信息");
            }
            String[] len = token.split(" ");
            if (len.length >=1){
                token = len[1];
                JWTUtil.verify(token);
                return true;
            }else {
                resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
                resultJson.put("msg", "无效签名信息");
            }

        } catch (SignatureVerificationException e) {
            e.printStackTrace();
            resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            resultJson.put("msg", "无效签名信息");
        } catch (AlgorithmMismatchException e) {
            e.printStackTrace();
            resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            resultJson.put("msg", "token算法不一致");
        } catch (Exception e) {
            e.printStackTrace();
            resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            resultJson.put("msg", "登录状态失效，请重新登录");
        }
        String s = JSON.toJSONString(resultJson);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(s);
        return false;
    }
}
