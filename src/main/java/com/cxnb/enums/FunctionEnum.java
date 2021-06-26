package com.cxnb.enums;

import lombok.Data;

/** 方法名集合 */
public enum FunctionEnum {
    lockaddress("lockaddress","锁地址的方法"),
    removeFuncName("remove","解锁地址的方法"),
    containsFuncName("contains","查看是否已被锁"),
    balanceOf("balanceOf","查看余额"),
    decimals("decimals","查看单位小数位数"),
    names("name","查看名称"),
    totalSupply("totalSupply","查看总额"),
    getlockvalues("getlockvalues","查看余额的方式");

    private String name;
    private String msg;

    FunctionEnum(String name,String msg){
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
