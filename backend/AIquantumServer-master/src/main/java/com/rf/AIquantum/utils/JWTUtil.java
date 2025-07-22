package com.rf.AIquantum.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rf.AIquantum.user.dao.model.UserEntity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lpf
 * @description:
 * @date 2021/12/2820:18
 */
public class JWTUtil {

    private static final String SING = "xVWGEYTPF1hjnFt$HDZ0f^iet^^q@hZv";

    public static final String AUTH_HEADER_KEY = "Authorization";

    //token前缀
    public static final String TOKEN_PREFIX = "Bearer ";

    public static String getToken(Map<String, String> map) {

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 5);
        JWTCreator.Builder builder = JWT.create();
        map.forEach(builder::withClaim);
        String token = builder.withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(SING));
        return token;
    }

    public static String getTokenByUserInfo(UserEntity userEntity){
        HashMap<String, String> payload = new HashMap<>();
        payload.put("userId", userEntity.getId());
        payload.put("phone",userEntity.getPhone());
        payload.put("userName",userEntity.getUserName());
        payload.put("type", userEntity.getRoleType());
        return  getToken(payload);
    }

    /**
     * 验证token
     * @param token
     */
    public static String verifyToken(String token) throws Exception {
        try {
            return JWT.require(Algorithm.HMAC256(SING))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();
        } catch (TokenExpiredException e){
            throw new Exception("token已失效，请重新登录",e);
        } catch (JWTVerificationException e) {
            throw new Exception("token验证失败！",e);
        }
    }


    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }

}
