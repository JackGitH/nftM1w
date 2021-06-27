package com.cxnb.task;

import com.cxnb.entity.Chain;
import com.cxnb.entity.DropOne;
import com.cxnb.enums.AccountEnum;
import com.cxnb.enums.StatusEnum;
import com.cxnb.mapper.ChainMapper;
import com.cxnb.mapper.DropMapper;
import com.cxnb.service.EthereumInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Configuration
@EnableScheduling
public class RefreshTxHashStatus {


    @Resource
    ChainMapper chainMapper;

    @Resource
    EthereumInfoService ethereumInfoService;

    @Resource
    DropMapper dropMapper;

    private static boolean flag  = false;

    @Scheduled(cron = "10 * * * * ?") // 每10秒检查一次
    public void getTxStatus() throws InterruptedException, ExecutionException {
        if(false){
            log.info("##正在执行刷新交易状态中");
            return;
        }
        log.info("##开始执行刷新交易状态");
        flag = true;
        List<Chain> chians = chainMapper.list(AccountEnum.status0.getStatus());
        for (Chain chain : chians) {

            List<DropOne> dropOneList1 = dropMapper.list(StatusEnum.status1.getStatus(), chain.getId(), "", "");

            log.info("chain:{} pending size:{}", chain.getTokenname(), dropOneList1.size());
            Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));
            for (DropOne dropOne : dropOneList1) {
                String status = ethereumInfoService.getEthTransactionReceipt(web3j, dropOne.getTxhash()).getStatus();
                if(status == null){
                    DropOne updateS = DropOne.builder()
                            .txstatus(StatusEnum.status3.getStatus())
                            .id(dropOne.getId())
                            .build();
                    dropMapper.update(updateS);
                    continue;
                }
                switch (status) {

                    case "0x0":
                        DropOne updateF = DropOne.builder()
                                .txstatus(StatusEnum.status3.getStatus())
                                .id(dropOne.getId())
                                .build();
                        dropMapper.update(updateF);
                        break;

                    case "0x1":
                        DropOne updateS = DropOne.builder()
                                .txstatus(StatusEnum.status2.getStatus())
                                .id(dropOne.getId())
                                .build();
                        dropMapper.update(updateS);
                        break;

                    default:
                        log.info("## hahs:{} is pending", dropOne.getTxhash());

                }
            }
        }
        flag = false;
    }
}
