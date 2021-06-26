package com.cxnb.exception;


import com.cxnb.enums.StatusCodeEnum;

@SuppressWarnings("serial")
public class IllegalParameterException extends BaseException {
    public IllegalParameterException() {
    }

    public IllegalParameterException(Throwable ex) {
        super(ex);
    }

    public IllegalParameterException(String message) {
        super(message);
    }

    public IllegalParameterException(StatusCodeEnum statusCode) {
        super(statusCode);
    }
    public IllegalParameterException(StatusCodeEnum statusCode, String message) {
        super(statusCode, message);
    }
    public IllegalParameterException(String message, Throwable ex) {
        super(message, ex);
    }

    @Override
    protected StatusCodeEnum getStatusCode() {
        return super.statusCode != null ? super.statusCode : StatusCodeEnum.FAIL;
    }
}
