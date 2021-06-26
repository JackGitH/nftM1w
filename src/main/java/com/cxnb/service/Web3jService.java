package com.cxnb.service;

import com.cxnb.config.AppConfig;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
public class Web3jService {
    @Resource
    AppConfig config;

    private static Web3j web3j;

    @PostConstruct
    public void init() {
        web3j = Web3j.build(new HttpService(config.getHecoInfoUrl()));
    }

    public Web3j getWeb3j() {
        return web3j;
    }

    public void setWeb3j(Web3j web3j) {
        this.web3j = web3j;
    }
}
