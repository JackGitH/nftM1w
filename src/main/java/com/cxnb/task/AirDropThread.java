package com.cxnb.task;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cxnb.config.AppConfig;
import com.cxnb.entity.TransInfo;
import com.cxnb.entity.TransRes;
import com.cxnb.enums.FunctionEnum;
import com.cxnb.service.HecoService;
import com.cxnb.service.TransService;
import com.cxnb.service.Web3jService;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AirDropThread implements Runnable {

    private static final String success = "1";
    private static final String finish = "0";



    // 数据库操作
    private AppConfig config;

    private HecoService hecoService;
    private Web3jService web3jService;
    private TransService transService;
    private String dropAddr;




    private final Logger log = LoggerFactory.getLogger(getClass());

    public AirDropThread(String dropAddr,AppConfig config,HecoService hecoService,Web3jService web3jService,TransService transService) {
        this.config = config;
        this.dropAddr = dropAddr;
        this.hecoService = hecoService;
        this.web3jService = web3jService;
        this.transService = transService;
    }

    @SneakyThrows
    @Override
    public void run() {
        log.info("##do share");
        String contractAddr = config.getContractAddress();
        // 开始按照持币空投
            if ((dropAddr.toLowerCase()).equals(config.getPub().toLowerCase())) {
                return;
            }
           /* Integer minSec = 5000; // 5秒
            Integer maxSec = 600000; // 10分钟
            Random randomSec = new Random(); // 5秒钟-10分钟之间产生的随机数进行空投
            int scaleSec = randomSec.nextInt(maxSec) % (maxSec - minSec + 1) + minSec;
            log.info("##随机空投秒数是：{}",scaleSec);
            Thread.sleep(scaleSec);*/
            log.info("##begin#########################################");
            log.info("##begin airdrop address:{}", dropAddr);
            Integer min = 100;
            Integer max = 2000;
            Random random = new Random();
            int scale = random.nextInt(max) % (max - min + 1) + min;

            try{
                String hash = transService.tokenDeal(web3jService.getWeb3j(), config.getPub(), dropAddr, contractAddr, String.valueOf(scale), config.getPk(), Integer.valueOf(config.getDecimal()));
                if(null== hash){
                    log.info("hash is null dropAddr:{} and continue second",dropAddr);
                    hash = transService.tokenDealSecond(web3jService.getWeb3j(), config.getPub(), dropAddr, contractAddr, String.valueOf(scale), config.getPk(), Integer.valueOf(config.getDecimal()));
                }
                log.info("##airDrop success address:{} count:{} hash:{}", dropAddr, scale, hash);
                log.info("##end#########################################");
            }catch (Exception e){
                log.error("##airDrop faild address:{} count:{}", dropAddr, scale);
                e.printStackTrace();
            }

    }


}
