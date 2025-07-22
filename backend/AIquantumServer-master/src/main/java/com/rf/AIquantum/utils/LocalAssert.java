package com.rf.AIquantum.utils;

import com.alibaba.druid.util.StringUtils;

import java.util.Collection;

/**
 * @Description:断言工具类
 * @Author: mimang
 * @Date: 2024/9/6
 */
public abstract  class LocalAssert {
    public static void isTrue(boolean expression, String message) throws RuntimeException {
        if (!expression) {
            throw new RuntimeException(message);
        }
    }
    public static void isStringEmpty(String param, String message) throws RuntimeException{
        if(StringUtils.isEmpty(param)) {
            throw new RuntimeException(message);
        }
    }

    public static void isObjectEmpty(Object object, String message) throws RuntimeException {
        if (object == null) {
            throw new RuntimeException(message);
        }
    }

    public static void isCollectionEmpty(Collection coll, String message) throws RuntimeException {
        if (coll == null || (coll.size() == 0)) {
            throw new RuntimeException(message);
        }
    }
}
