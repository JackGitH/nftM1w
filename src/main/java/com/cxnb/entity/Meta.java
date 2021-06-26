package com.cxnb.entity;

import java.io.Serializable;

/**
 * @desc: 消息头
 */
public class Meta implements Serializable {
    private static final long serialVersionUID = -6190165129774916252L;
    /** 状态码 **/
    private Integer code;
    /** 消息 **/
    private String msg;

    public Meta() {
    }

    public Meta(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
