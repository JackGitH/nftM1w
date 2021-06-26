package com.cxnb.controller;


import com.cxnb.entity.Response;
import com.cxnb.enums.StatusCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * @desc:
 */
public abstract class BaseController {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    private StatusCodeEnum statusCodeEnum;

    //---------------- 成功 --------------------
    protected ResponseEntity<Response> success() {

        return setResponse(statusCodeEnum.OK.code(), statusCodeEnum.OK.msg(), null);
    }

    protected ResponseEntity<Response> success(Object data) {
        return setResponse(statusCodeEnum.OK.code(), statusCodeEnum.OK.msg(), data);
    }

    protected ResponseEntity<Response> success(String msg, Object data) {
        return setResponse(statusCodeEnum.OK.code(), msg, data);
    }

    //---------------- 失败 --------------------
    protected ResponseEntity<Response> failure() {
        return setResponse(statusCodeEnum.FAIL.code(), statusCodeEnum.FAIL.msg(), null);
    }

    protected ResponseEntity<Response> failure(String msg) {
        return setResponse(statusCodeEnum.FAIL.code(), statusCodeEnum.FAIL.msg(), msg);
    }

    protected ResponseEntity<Response> failure(Object data) {
        return setResponse(statusCodeEnum.FAIL.code(), statusCodeEnum.FAIL.msg(), data);
    }

    protected ResponseEntity<Response> failure(StatusCodeEnum codeEnum) {
        return setResponse(codeEnum.code(), codeEnum.msg(), null);
    }

    protected ResponseEntity<Response> failure(Integer statusCode, String msg) {
        return setResponse(statusCode, msg, null);
    }

    /**
     * 响应报文
     *
     * @param code 状态码
     * @param msg        消息
     * @param data       数据
     * @return 响应实体
     */
    private ResponseEntity<Response> setResponse(Integer code, String msg, Object data) {
        return ResponseEntity.ok(new Response(code, msg, data));
    }
}
