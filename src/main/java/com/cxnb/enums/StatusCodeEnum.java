package com.cxnb.enums;

/**
 * Desc: Ajax 请求时的自定义查询状态码
 */
public enum StatusCodeEnum {
    /** 请求成功 */
    OK(0, "执行成功"),
    FAIL(1400, "执行失败"),
    ARGS_ERROR(5101, "参数错误"),
    ARGS_NOT_EMPTY(5102, "参数不能为空"),

    /**登录操作**/
    POWER_NOT_ENOUGH(1900, "权限不够"),


    /**区块链浏览器右上角四个展示数据存储在redis中的key **/
    BAAS_FOUR_1(1, "BAAS_FOUR1_SIZE"),
    BAAS_FOUR_2(2, "BAAS_FOUR2_BLOCK_HEIGTH"),
    BAAS_FOUR_3(3, "BAAS_FOUR3_CHANNEL_COUNT"),
    BAAS_FOUR_4(4, "BAAS_FOUR4_SMART_CONTRACT"),
    BAAS_FOUR_5(5, "BAAS_FOUR5_BLOCK_HEIGHT_FORTX"), // 这个要区分channelid 使用的时候注意+channelid
    BAAS_FOUR_6(6, "BAAS_FOUR6_BLOCK_HEIGHT_FORTX_COUNT"), //这个要区分channelid 使用的时候注意+channelid


    /**Channel表操作**/
    CHANNEL_STATUS_1(1, "创建中"),
    CHANNEL_STATUS_2(2, "已创建"),
    CHANNEL_STATUS_3(3, "创建失败"),


    /**Orderer表操作**/
    ORDERER_STATUS_1(1, "未启动"),
    ORDERER_STATUS_2(2, "已启动"),

    /**Peer表操作**/
    PEER_STATUS_1(1, "未启动"),
    PEER_STATUS_2(2, "已启动"),
    PEER_STATUS_3(-1, "已删除"),


    /**ChannelOrg表操作**/
    CHANNELORG_STATUS_1(1, "未加入"),
    CHANNELORG_STATUS_2(2, "已加入"),
    CHANNELORG_STATUS_3(3 , "审核中"),
    CHANNELORG_STATUS_4(4, "审核失败"),


    /**ChaincodeOrg表操作**/
    CHAINCODEORG_FINALSTATUS_1(1, "安装完成"),
    CHAINCODEORG_FINALSTATUS_2(2, "安装失败"),




    /** 企业审核相关 */
    EMAIL_FORMAT_ERROR(5104, "Email格式不正确"),


    /**用户模块响应码*/
    USER_INNER_ERROR(3000, "用户模块操作异常"),
    USER_NOT_EXIST(3001, "用户不存在或状态异常"),
    PASSWORD_NOT_CORRECT(3002, "用户密码不正确"),
    USER_ALREADY_LOGIN(3003, "用户已经登录"),
    TOKEN_NOT_CORRECT(3004, "用户Token不正确"),
    TOKEN_EXPIRE(3005, "登录过期"),
    USER_INVALID(3006, "无效的用户"),
    USER_EXIST(3007, "用户已存在"),
    ROLE_TX_NOT_FOUND(3008, "系统中没有交易角色"),
    MOBILE_FORMAT_ERROR(3009, "手机号格式不正确"),
    USER_MOBILE_EXIST(4001, "该手机号已被注册"),
    USER_MOBILE_SAME(4002, "输入手机号与用户注册手机号不相符"),
    /**账户模块响应码*/
    ACCOUNT_INNER_ERROR(4000, "账户信息操作异常"),
    ACCOUNT_CHAIN_ERROR(4010, "账户信息与所属链不匹配"),
    ACCOUNT_NOT_FOUND(4040, "没有该帐户"),
    ACCOUNT_TXS_OUT_NOT_FOUND(4041, "没有该输入帐户"),
    ACCOUNT_TXS_IN_NOT_FOUND(4042, "没有该输出帐户"),
    ACCOUNT_STATE_ABNORMAL(4050, "帐户状态异常"),
    ACCOUNT_TXS_OUT_STATE_ABNORMAL(4051, "输入帐户状态异常"),
    ACCOUNT_TXS_IN_STATE_ABNORMAL(4052, "输出帐户状态异常"),
    AMOUNT_BIG(4053, "超过最大金额"),


    /**
     * 交易结果集查询
     */
    TXRESULT_NOT_EXIST(6001, "交易结果集查询异常"),
    /**应用模块相关响应码*/



    SDK_ERROR(7000, "调用SDK出错"),
    UCHAINS_ERROR(8000, "业务出错"),

    HTTP_REQUEST_ERROR(6000, "请求出错"),

    /** 业务出错 **/
    RESPONSE_ERROR(9002, "请求响应返回错误"),
    DATA_ERROR(9003, "不是标准的JSON对象"),
    BLANK_ERROR(9004, "底层返回为空"),
    SIGN_ERROR(9100, "签名出错"),

    CHAINID_HAS_EXIST_ERROR(9101, "链ID已存在"),
    CHAINID_NO_EXIST_ERROR(9102, "链ID不存在"),
    APP_NO_EXIST_ERROR(9103, "应用编号已存在"),
    USER_NO_OPERATION_PERMISSION_ERROR(9104, "您无此操作权限"),
    APP_MAP_CHAINID_HAS_EXISTS_ERROR(9105, "应用与链关系已存在"),
    CHAINID_STATE_ERROR(9106, "链状态有误"),
    APPNO_NOT_EXISTS_ERROR(9107, "应用编号不存在"),

    /**
     * Baaserver 服务出错
     */
    REQUEST_BAASERVER_ERROR(9999, "调用Baas服务出错"),
    REQUEST_APP_STATE_ABNORMAL(9998,"应用不可用，请确定应用当前状态是否可用"),



    FABRIC_NODE_NOT_ALIVE(9220, "节点未启动"),

    FABRIC_CHANNEL_CREATE_FAIL(9230, "通道创建失败"),
    FABRIC_CHANNEL_INSTANTIATE_FAIL(9231, "通道实例化失败"),


    FABRIC_CHAINCODE_INSTALL_FAIL(9240, "链码安装失败"),
    FABRIC_CHAINCODE_UPLOAD_FAIL(9241, "链码上传失败");



    private final Integer code;
    private final String message;

    StatusCodeEnum(Integer value, String message) {
        this.code = value;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String msg() {
        return this.message;
    }

}