package com.cxnb.entity;


public class Order {

    public Order(String paycode) {
        this.paycode = paycode;
    }

    private String paycode;

    public Order() {

    }
    public String getPaycode() {
        return paycode;
    }

    public void setPaycode(String paycode) {
        this.paycode = paycode;
    }


    @Override
    public String toString() {
        return "Order{" +
                "paycode='" + paycode + '\'' +
                '}';
    }
}
