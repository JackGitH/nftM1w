package com.cxnb.exception;


import com.cxnb.entity.Meta;
import com.cxnb.enums.StatusCodeEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * @desc:
 */
@SuppressWarnings("serial")
public abstract class BaseException extends RuntimeException {

    protected StatusCodeEnum statusCode;

    public BaseException() {
    }

    public BaseException(Throwable ex) {
        super(ex);
    }

    public BaseException(String message) {
        this(message, null);
    }

    public BaseException(StatusCodeEnum statusCode) {
        this(statusCode.msg(), null);
        this.statusCode = statusCode;
    }

    public BaseException(StatusCodeEnum statusCode, String message) {
        this(message, null);
        this.statusCode = statusCode;
    }

    public BaseException(String message, Throwable ex) {
//		super(message, ex, false, false);
        super(message, ex, true, true);
    }

    public void handler(Meta meta) {
        meta.setCode(getStatusCode().code());
        if (StringUtils.isNotBlank(getMessage())) {
            meta.setMsg(getMessage()); // 取系统的错误消息
        }else {
            meta.setMsg(getStatusCode().msg());
        }
    }

    protected abstract StatusCodeEnum getStatusCode();
}
