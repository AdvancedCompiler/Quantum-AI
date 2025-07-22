package com.rf.AIquantum.base.rest;

import com.rf.AIquantum.utils.HttpStatus;
import com.rf.AIquantum.utils.Result;


/**
 * @author zzf
 * @description:
 * @date 2021/1/18 19:20
 */
public class BaseController {
    protected static final String PAGE_NUM = "pageNum";

    protected static final String PAGE_SIZE = "pageSize";

    protected Result success() {
        return success(null, "成功");
    }

    protected static <T> Result<T> success(T data) {
        return success(data, "成功");
    }

    protected static <T> Result<T> success(T data, String message) {
        if (null == data) {
            return new Result<>(HttpStatus.SUCCESS, message);
        }
        return new Result<>(HttpStatus.SUCCESS, message, data);
    }

    protected static <T> Result<T> success(String code,T data, String message) {
        if (null == data) {
            return new Result<>(code, message);
        }
        return new Result<>(code, message, data);
    }

    protected Result fail() {
        return fail(null, "失败");
    }

    protected static <T> Result<T> fail(T data) {
        return fail(data, "失败");
    }

    protected static <T> Result<T> fail(String message) {
        return new Result<>(HttpStatus.RUNTIME_EXCEPTION, message);
    }

    protected static <T> Result<T> noAuth(String message) {
        return new Result<>(HttpStatus.NO_AUTH_ERROR, message);
    }

    protected static <T> Result<T> fail(String code ,T data ,String message){
        if(data == null)
            return new Result<>(code,message);
        else
            return new Result<>(code,message,data);
    }

    protected static <T> Result<T> fail(T data, String message) {
        if (null == data) {
            return new Result<>(HttpStatus.RUNTIME_EXCEPTION, message);
        }
        return new Result<>(HttpStatus.RUNTIME_EXCEPTION, message, data);
    }

    protected Result failBadRequest() {
        return failBadRequest(null, "参数异常");
    }

    protected static <T> Result<T> failBadRequest(T data) {
        return failBadRequest(null, "参数异常");
    }

    protected static <T> Result<T> failBadRequest(T data, String message) {
        return new Result<>(org.springframework.http.HttpStatus.BAD_REQUEST.value() + "", message, data);
    }
}
