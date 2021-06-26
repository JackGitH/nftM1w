package com.cxnb.enums;

/** 状态集合 */
public enum AccountEnum {
    status1(1,"正常"),
    status0(0,"所有"),
    status2(2,"锁定"),;

    private Integer status;
    private String msg;

    AccountEnum(Integer status, String msg){
        this.status = status;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
