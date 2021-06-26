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
import jnr.ffi.annotations.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Configuration
@EnableScheduling
@Slf4j
public class AirDrop {


    private Boolean flagedrop = false;
    private static final String success = "1";
    private static final String finish = "0";
    @Resource
    AppConfig config;
    @Resource
    HecoService hecoService;
    @Resource
    TransService transService;
    @Resource
    Web3jService web3jService;
    @Resource
    AirDropThreadPool pool;


    @Scheduled(cron = "0 */1 * * * ?") // 每一分钟空投一次
    public void shareToken() throws InterruptedException {
        log.info("#before do share flag:{}", flagedrop);
        if (flagedrop) {
            log.info("##do share");
            String contractAddr = config.getContractAddress();
            //String contractAddr2 = "0xa71edc38d189767582c38a3145b5873052c3e47a"; // usdt
            //String contractAddr2 = "0xD2e860D923c997764b229f6F397CC2Cc5d8c9aFf"; // ONB
            //String contractAddr2 = "0x66a79d23e58475d2738179ca52cd0b41d73f0bea"; // HBTC
            String contractAddr2 = "0xa47eda5667fd87738dd397cf9ed6a0d34cde818d"; // Uranus
            String apiKey = config.getApikey();

            Set<String> addrSet = new HashSet<>();
            Set<String> holdAddrSet = new HashSet<>();
            Set<String> dropAddrSet = new HashSet<>();
            boolean totalFlag = true;
            boolean holdFlag = true;
           // Integer page = 13;
            Integer page = 1;
            Integer pageHold= 1;
            Integer offset = 500;
            while (totalFlag) {
               /* if (page >= 50) {
                    break;
                }*/
                if (page >= 20) {
                    break;
                }
                String url = "https://api.hecoinfo.com/api?module=account&action=tokentx&contractaddress=" + contractAddr2 + "&page=" + page + "&offset=" + offset + "&sort=asc&apikey=" + apiKey;//指定URL
                //发送get请求并接收响应数据
                String result = HttpUtil.createGet(url).execute().body();
                TransRes transRes = JSONObject.parseObject(result, TransRes.class);
                if (success.equals(transRes.getStatus())) {
                    page++;
                    log.info("##交易列表:{}", transRes.getResult().size());
                    for (TransInfo e : transRes.getResult()) {
                        addrSet.add(e.getTo());
                    }
                } else if (finish.equals(transRes.getStatus())) {
                    log.info("##调用接口查询数据完成 url:{}", url);
                    totalFlag = false;
                } else {
                    log.error("##调用接口查询失败 url:{}", url);
                    totalFlag = false;
                }
            }


            while (holdFlag) {
                String url = "https://api.hecoinfo.com/api?module=account&action=tokentx&contractaddress=" + contractAddr + "&page=" + pageHold + "&offset=" + offset + "&sort=asc&apikey=" + apiKey;//指定URL
                //发送get请求并接收响应数据
                String result = HttpUtil.createGet(url).execute().body();
                TransRes transRes = JSONObject.parseObject(result, TransRes.class);
                if (success.equals(transRes.getStatus())) {
                    pageHold++;
                    log.info("##本地token交易列表:{}", transRes.getResult().size());
                    for (TransInfo e : transRes.getResult()) {
                        holdAddrSet.add(e.getTo());
                    }
                } else if (finish.equals(transRes.getStatus())) {
                    log.info("##本地token调用接口查询数据完成 url:{}", url);
                    holdFlag = false;
                } else {
                    log.error("##本地token调用接口查询失败 url:{}", url);
                    holdFlag = false;
                }
            }


            log.info("##不重复的交易地址总量有：{}", addrSet.size());
            log.info("##本地token不重复的交易地址总量有：{}", holdAddrSet.size());

            for (String addr : addrSet) {
                if (!holdAddrSet.contains(addr)) {
                    dropAddrSet.add(addr);
                }
            }
            log.info("##最终需要空投的地址总量有：{}", dropAddrSet.size());
            // 开始按照持币空投
            for (String addr : dropAddrSet) {
               /* if ((addr.toLowerCase()).equals(config.getPub().toLowerCase())) {
                    continue;
                }
                Integer minSec = 5000; // 5秒
                Integer maxSec = 600000; // 10分钟
                Random randomSec = new Random(); // 5秒钟-10分钟之间产生的随机数进行空投
                int scaleSec = randomSec.nextInt(maxSec) % (maxSec - minSec + 1) + minSec;
                log.info("##随机空投秒数是：{}",scaleSec);
                Thread.sleep(scaleSec);
                log.info("##begin#########################################");
                log.info("##begin airdrop address:{}", addr);
                Integer min = 100;
                Integer max = 2000;
                Random random = new Random();
                int scale = random.nextInt(max) % (max - min + 1) + min;

                String hash = transService.tokenDeal(web3jService.getWeb3j(), config.getPub(), addr, contractAddr, String.valueOf(scale), config.getPk(), Integer.valueOf(config.getDecimal()));
                log.info("##airDrop success address:{} count:{} hash:{}", addr, scale, hash);
                log.info("##end#########################################");*/
                Thread.sleep(100);
                pool.pushMsh(addr);
            }
        }
    }


    public Boolean getFlagedrop() {
        return flagedrop;
    }

    public void setFlagedrop(Boolean flagedrop) {
        this.flagedrop = flagedrop;
    }


}
