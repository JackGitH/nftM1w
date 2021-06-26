package com.cxnb.entity;

import java.io.Serializable;

/**
 * @desc: 返回的实体
 */
public class Response implements Serializable {
    private static final long serialVersionUID = -5363764246862129495L;
    private Meta meta;
    /** 数据体 **/
    private Object data;

    public Response() {
    }

    public Response(Meta meta, Object data) {
        this.meta = meta;
        this.data = data;
    }

    public Response(Integer code, String msg, Object data) {
        this.meta = new Meta(code, msg);
        this.data = data;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
