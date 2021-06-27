package com.cxnb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value="Chain",description="chain信息")
public class Chain {
    @ApiModelProperty(value="1",name="id")
    private Long id;// 1 heco 2 bsc 3 okt


    @ApiModelProperty(value="1",name="tokenname")
    private String tokenname;// HT BNB OKN等资产名称


    @ApiModelProperty(example="https://http-mainnet-node.huobichain.com",name="url")
    private String url;

    @ApiModelProperty(example="XDPZWVYIT4W21YY3DZNWBUUDQB1X6S55MY",name="apikey")
    private String apikey; // 区块链浏览器里的apikey

    @ApiModelProperty(example="1",name="status")
    private String status; // 1 正常 2 冻结

    @ApiModelProperty(example="0.01",name="rate")
    private String rate; // 费率  比如 0.01

    @ApiModelProperty(example="2021-06-20 12:12:12",name="modtime")
    private Date modtime;

    @ApiModelProperty(example="2021-06-20 12:12:12",name="createtime")
    private Date createtime;


    private String priceurl;
    private String recipientaddress;
    private String chainname;
}
