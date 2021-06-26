package com.cxnb.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * heco:
 *   contract-address: 0x0541598c076cebc06774AfF6fB4268Fb2d9AfD2a
 *   environment-url: https://http-mainnet-node.huobichain.com
 *   pk: 0x3fd999dd4df50509f9eba89736fd66e22343c85229eaea24257292684e7f07ca
 *   pub: 0x202108aB04f1EE44781AC962a41f26c14d07Ca3F
 *   apikey: XDPZWVYIT4W21YY3DZNWBUUDQB1X6S55MY
 */
@Configuration
@Slf4j
@Data
public class AppConfig {
    @Value("${heco.token.contract-address}")
    private String contractAddress;

    @Value("${heco.environment-url}")
    private String hecoInfoUrl;

    @Value("${heco.pk}")
    private String pk;

    @Value("${heco.pub}")
    private String pub;

    @Value("${heco.apikey}")
    private String apikey;

    @Value("${heco.token.total}")
    private String total;

    @Value("${heco.token.decimal}")
    private String decimal;

    @Value("${heco.token.name}")
    private String name;

    @Value("${heco.token.share}")
    private String share;

    @Value("${heco.token.airdrop}")
    private String airdrop;


    @Value("${heco.toppath}")
    private String toppath;


    @Value("${heco.tokenpath}")
    private String tokenpath;


}
