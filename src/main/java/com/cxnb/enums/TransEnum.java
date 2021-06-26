package com.cxnb.enums;

public enum TransEnum {
    success("0x1","success"),
    fail("0x0","faild"),;

    private String code;
    private String msg;

    TransEnum(String code, String msg){
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
