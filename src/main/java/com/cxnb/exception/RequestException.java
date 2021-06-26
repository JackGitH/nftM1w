package com.cxnb.exception;


import com.cxnb.enums.StatusCodeEnum;

/**
 * @desc:
 */
public class RequestException extends BaseException {

    public RequestException() {
        super();
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException(StatusCodeEnum statusCode) {
        super(statusCode);
    }
    public RequestException(StatusCodeEnum statusCode, String message) {
        super(statusCode, message);
    }
    public RequestException(String message, Throwable ex) {
        super(message, ex);
    }

    @Override
    protected StatusCodeEnum getStatusCode() {
        return super.statusCode != null ? super.statusCode : StatusCodeEnum.FAIL;
    }
}