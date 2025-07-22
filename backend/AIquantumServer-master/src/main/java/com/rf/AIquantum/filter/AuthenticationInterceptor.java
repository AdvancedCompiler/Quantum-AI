package com.rf.AIquantum.filter;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.rf.AIquantum.filter.JwtIgnore;
import com.rf.AIquantum.utils.JWTUtil;
import com.rf.AIquantum.utils.LocalAssert;
import com.rf.AIquantum.utils.WebContextUtil;
import io.swagger.models.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @Description:使用AuthenticationInterceptor拦截器对接口参数进行验证
 * @Author: mimang
 * @Date: 2024/9/6
 */
@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HashMap<String, Object> resultJson = new HashMap<>();
        final String url = request.getRequestURI();
        log.info("url-------------------"+url);
        // 从http请求头中取出token
        final String token = request.getHeader(JWTUtil.AUTH_HEADER_KEY);
        //如果不是映射到方法，直接通过
        if(!(handler instanceof HandlerMethod)){
            return true;
        }
        //如果是方法探测，直接通过
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }
        //如果方法有JwtIgnore注解，直接通过
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method=handlerMethod.getMethod();
        if (method.isAnnotationPresent(JwtIgnore.class)) {
            JwtIgnore jwtIgnore = method.getAnnotation(JwtIgnore.class);
            if(jwtIgnore.value()){
                return true;
            }
        }

        try {
            LocalAssert.isStringEmpty(token, "token为空，鉴权失败！");
            //验证，并获取token内部信息
            String userToken = JWTUtil.verifyToken(token);
            //将token放入本地缓存
            WebContextUtil.setUserToken(userToken);
            return true;
        }catch (SignatureVerificationException e) {
            e.printStackTrace();
            resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            resultJson.put("msg", "无效签名信息");
        } catch (AlgorithmMismatchException e) {
            e.printStackTrace();
            resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            resultJson.put("msg", "token算法不一致");
        }catch (TokenExpiredException e){
            e.printStackTrace();
            resultJson.put("code", HttpServletResponse.SC_UNAUTHORIZED);
            resultJson.put("msg", "token已过期，请重新获取");
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //方法结束后，移除缓存的token
        WebContextUtil.removeUserToken();
    }
}

