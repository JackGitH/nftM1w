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
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

;import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableScheduling
@Slf4j
public class Share {


    private Boolean flage = false;
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

    @Scheduled(cron = "0 0 1 ? * FRI") // 每周五凌晨一点分红
    //@Scheduled(cron = "0 0 12 * * ? ") // 每天12点分红
   // @Scheduled(cron = "0 */1 * * * ?") // 每一分钟分红一次
    public void shareToken() throws InterruptedException {
        log.info("#before do share flag:{}",flage);
        if (flage) {
            log.info("##do share");
            String contractAddr = config.getContractAddress();
            Integer decimal  = Integer.valueOf(config.getDecimal());
            //contractAddr = "0xD2e860D923c997764b229f6F397CC2Cc5d8c9aFf";
            String apiKey = config.getApikey();
            //apiKey = "XDPZWVYIT4W21YY3DZNWBUUDQB1X6S55MY";

            Set<String> addrSet = new HashSet<>();
            boolean totalFlag = true;
            Integer page = 1;
            Integer offset = 500;
            while (totalFlag) {
                Thread.sleep(300);
                String url = "https://api.hecoinfo.com/api?module=account&action=tokentx&contractaddress=" + contractAddr + "&page=" + page + "&offset=" + offset + "&sort=asc&apikey=" + apiKey;//指定URL
                //发送get请求并接收响应数据
                String result = HttpUtil.createGet(url).execute().body();
                TransRes transRes = JSONObject.parseObject(result, TransRes.class);
                if (success.equals(transRes.getStatus())) {
                    page++;
                    log.info("##交易列表:{}",transRes.getResult().size());
                    for (TransInfo e: transRes.getResult()) {
                        addrSet.add(e.getTo());
                    }
                } else if(finish.equals(transRes.getStatus())){
                    log.info("##调用接口查询数据完成 url:{}",url);
                    totalFlag   = false;
                }else {
                    log.error("##调用接口查询失败 url:{}",url);
                    totalFlag   = false;
                }
            }
            log.info("##不重复的交易地址总量有：{}",addrSet.size());

            // 开始按照持币比例分红
            for(String addr:addrSet){
                Thread.sleep(50);
                if((addr.toLowerCase()).equals(config.getPub().toLowerCase())){
                    continue;
                }
                if((addr.toLowerCase()).equals("0xd05d40fb33ddc6580ec4cc455c3778710afb1dce")){ // 一个特殊的账号
                    continue;
                }
                log.info("##begin#########################################");
                log.info("##begin share address:{}",addr);
                BigInteger balance = hecoService.greyFucn(addr, FunctionEnum.balanceOf.getName());
                double dvBalance = balance.doubleValue();

                double pow =dvBalance / (Math.pow(10.00, decimal));
                double share = Double.valueOf(config.getShare());
                double scale = pow * share; // 分红总数的0.06
                String hash = transService.tokenDeal(web3jService.getWeb3j(), config.getPub(), addr, contractAddr, String.valueOf(scale), config.getPk(), Integer.valueOf(config.getDecimal()));
                log.info("##share success address:{} count:{} hash:{}",addr,scale,hash);
                log.info("##end#########################################");
            }
        }
    }

    public Boolean getFlage() {
        return flage;
    }

    public void setFlage(Boolean flage) {
        this.flage = flage;
    }

}
