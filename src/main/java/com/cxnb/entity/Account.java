package com.cxnb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "Account", description = "账户信息")

public class Account {
    @ApiModelProperty(example = "0x8f3a44e08c8ef32d0f20e92146093c0e1e4008e714bd80ac407fabd57c37b0ed", name = "pk")
    private String pk; // 私钥

    @ApiModelProperty(example = "0x3f2c646b8a1f687c12051c9b425101194fd8615b", name = "pub", required = true)
    private String pub; // 地址

    @ApiModelProperty(example = "1", name = "status")
    private Integer status;// 1 正常  2冻结

    @ApiModelProperty(example = "2021-06-20 12:12:12", name = "modtime")
    private String modtime;

    @ApiModelProperty(example = "2021-06-20 12:12:12", name = "createtime")
    private String createtime;

    /*** ---------------------上边是account原生，下边是空投传参前后台使用---------------------- */

    // 传递给空投使用如下：
    private String tokenname;//资产名称
    private String balance; //资产余额

    @ApiModelProperty(example = "1", name = "chainid", required = true)
    private Long chainid;//

    @ApiModelProperty(example = "ONB", name = "contractname", required = true)
    private String contractname; //合约

    private String contractbalance;//合约余额

    @ApiModelProperty(example = "0xhjakjdajkdkada", name = "contractaddress", required = true)
    private String contractaddress;//合约地址

    @ApiModelProperty(example = "9", name = "decimal", required = true)
    private Integer decimal; //小数位数

    @ApiModelProperty(example = "0x3f2c646b8a1f687c12051c9b425101194fd8615b", name = "toaddress", required = true)
    private String toaddress; // 被空投的地址

    @ApiModelProperty(example = "100", name = "amount", required = true)
    private String amount;//空投数量

    private Integer pageNum;
    private Integer pageSize;
    private List<String> toaddressList;



}
