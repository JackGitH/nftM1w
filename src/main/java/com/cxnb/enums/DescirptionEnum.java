package com.cxnb.enums;

/**描述信息*/
public enum DescirptionEnum {

    balanceNotEnough("1","余额不足，请充值。"),
    fail("3","操作失败，请重新执行。"),
    dosucc("6","操作成功 address："),
    removesuc("4","移除地址成功。"),
    removefail("5","移除失败，请重新移除。"),
    dontoperation("2","锁定地址中，请勿操作。");

    private String name;
    private String msg;

    DescirptionEnum(String name,String msg){
        this.name = name;
        this.msg = msg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
