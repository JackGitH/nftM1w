package com.cxnb.enums;

public enum StatusEnum {
    status0(0,"all"),
    status1(1,"pending"),
    status2(2,"success"),
    status3(3,"fail"),;

    private Integer status;
    private String msg;

    StatusEnum(Integer status, String msg){
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
