package com.cxnb.exception;

import com.cxnb.controller.BaseController;
import com.cxnb.entity.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异常处理
 */
@ControllerAdvice
@Slf4j
public class ExceptionResHandler extends BaseController {
    //自定义异常返回对应编码
    @ExceptionHandler(com.cxnb.exception.RequestException.class)
    @ResponseBody
    public ResponseEntity<Response> handlerRequestException(com.cxnb.exception.RequestException e) {
        log.warn("##请求异常");
        return failure(e.getMessage());
    }


    //自定义异常返回对应编码
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<Response> handlerIllegalArgumentException(IllegalArgumentException e) {
        log.warn("##请求参数异常");
        return failure(e.getMessage());
    }
}
