package com.cxnb.task;

import com.cxnb.config.AppConfig;
import com.cxnb.service.HecoService;
import com.cxnb.service.TransService;
import com.cxnb.service.Web3jService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.*;

@Service
@Slf4j
public class AirDropThreadPool {
    // 线程池维护线程的最少数量
    private final static int CORE_POOL_SIZE = 2;
    // 线程池维护线程的最大数量
    private final static int MAX_POOL_SIZE = 5;
    // 线程池维护线程所允许的空闲时间
    private final static int KEEP_ALIVE_TIME = 0;
    // 线程池所使用的缓冲队列大小
    private final static int WORK_QUEUE_SIZE = 5000;

    @Resource
    AppConfig config;
    @Resource
    HecoService hecoService;
    @Resource
    TransService transService;
    @Resource
    Web3jService web3jService;


    /**
     * 拒绝策略
     */
    final RejectedExecutionHandler handler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.warn("## beugin rejectedExecution");
        }
    };

    final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
            TimeUnit.SECONDS, new ArrayBlockingQueue(WORK_QUEUE_SIZE), this.handler);


    /**
     * 线程池新增推送
     */
    public void pushMsh(String dropAddr) {
        log.info("add new airDrop {}", dropAddr);
        threadPool.execute(new AirDropThread(dropAddr,config, hecoService, web3jService, transService));
    }



    /**
     * 线程池的定时任务---->周期性检测是否有缓存队列 并进行执行线程。 15秒检测一次
     */
    final ScheduledExecutorService schedulerPool = Executors.newScheduledThreadPool(10);
    final ScheduledFuture scheduledFuture = schedulerPool.scheduleAtFixedRate(new Runnable() {
        @Override
        public void run() {
        }
    }, 0, 15, TimeUnit.SECONDS);
}
