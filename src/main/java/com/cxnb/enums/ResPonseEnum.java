package com.cxnb.enums;

/** 前台返回码*/
public enum ResPonseEnum {

    success("0","success"),
    fail("500","fail");

    private String code;
    private String msg;
    ResPonseEnum(String code,String msg){
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
