package com.cxnb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@ApiModel(value="DropOne",description="空投")
public class DropOne {
    private Long id;

    @ApiModelProperty(example="0x3f2c646b8a1f687c12051c9b425101194fd8615b",name="from")
    private String fromaddress; // 发出账户

    @ApiModelProperty(example="0x3f2c646b8a1f687c12051c9b425101194fd8615b",name="from")
    private String toaddress; // 接收账户

    @ApiModelProperty(example="100",name="amount")
    private String amount; // 空投数量

    @ApiModelProperty(example="0x66dD43C03fa91aBeCb8F4EE2F7F34de9ed88eB26",name="contractaddress")
    private String contractaddress; // 合约地址

    @ApiModelProperty(example="0x263e757c4a17df1b7b0465dfc7df89be7ba2589d271db354479e41facabaa528",name="txhash")
    private String txhash; //

    @ApiModelProperty(example="2021-06-20 12:12:10",name="datetime")
    private String datetime; //


    @ApiModelProperty(example="1",name="txstatus")
    private Integer txstatus; //1 pending 2 success 3 fail


    @ApiModelProperty(example="1",name="chainid")
    private Long chainid; //链 id

    @ApiModelProperty(example="ONB",name="contractname")
    private String contractname; // 合约名称

    @ApiModelProperty(example = "2021-06-20 12:12:12", name = "modtime")
    private String modtime;

    @ApiModelProperty(example = "2021-06-20 12:12:12", name = "createtime")
    private String  createtime;


}
