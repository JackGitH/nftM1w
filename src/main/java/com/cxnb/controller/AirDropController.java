package com.cxnb.controller;


import com.cxnb.config.AppConfig;
import com.cxnb.entity.Account;
import com.cxnb.entity.Chain;
import com.cxnb.entity.DropOne;
import com.cxnb.entity.Response;
import com.cxnb.service.AirDropService;
import com.cxnb.util.Assert;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping(value = "/drop")
@Slf4j
public class AirDropController extends BaseController {
    @Resource
    AirDropService dropService;
    @Resource
    AppConfig appConfig;


    /**
     * 导入多账号
     */
    @PostMapping(value = "includes")
    @ResponseBody
    public ResponseEntity<Response> inAccountMore(@RequestParam List<String> pkSs, @RequestParam Long chainId, @RequestParam String contractAddress) throws Exception {
        log.info("##begin inAccountMore");
        List<Account> accounts = dropService.inAccounts(pkSs, chainId, contractAddress);
        return success(accounts);
    }

    /**
     * 导入单个账号
     */
    @PostMapping(value = "include")
    @ResponseBody
    public ResponseEntity<Response> inAccount(@RequestParam String pkS, @RequestParam Long chainId, @RequestParam String contractAddress) throws Exception {
        log.info("##begin inAccount");
        Account account = dropService.inAccount(pkS, chainId, contractAddress);

        return success(account);
    }

    /**
     * 单个空投
     */
    @PostMapping(value = "dropone")
    @ResponseBody
    public ResponseEntity<Response> dropone(@RequestBody Account account) throws IOException, ExecutionException, InterruptedException {
        log.info("##begin dropOne account:{}", account);
        PageInfo<DropOne> dropList = dropService.drop(account);
        return success(dropList);
    }


    /**
     * 批量空投
     */
    @PostMapping(value = "dropmore")
    @ResponseBody
    public ResponseEntity<Response> dropmore(@RequestBody Account account) throws IOException, ExecutionException, InterruptedException {
        log.info("##begin dropOne account:{}", account);
        dropService.dropmore(account);
        return success("操作成功，正在执行");
    }


    /**
     * 刷新结果 && 翻页
     */
    @PostMapping(value = "refresh")
    @ResponseBody
    public ResponseEntity<Response> refresh(@RequestBody Account account) throws IOException, ExecutionException, InterruptedException {
        log.info("##begin dropOne");
        Assert.notNull(account, "输入不能为空");
        String from = account.getPub();
        String contractaddress = account.getContractaddress();
        Integer pageNum = account.getPageNum();
        Integer pageSize = account.getPageSize();
        PageInfo<DropOne> dropList = dropService.refreshDropList(from, contractaddress, pageNum, pageSize);
        return success(dropList);
    }

    /**
     * 刷新结果带出统计
     */
    @PostMapping(value = "droptotal")
    @ResponseBody
    public ResponseEntity<Response> refreshDropTotal(@RequestBody Account account) throws IOException, ExecutionException, InterruptedException {
        log.info("##begin dropOne");
        Assert.notNull(account, "输入不能为空");
        String from = account.getPub();
        String contractaddress = account.getContractaddress();
        String result = dropService.refreshDropTotal(from, contractaddress);
        return success(result);
    }

    /**
     * 刷新eth余额
     */
    @PostMapping(value = "check/balance")
    @ResponseBody
    public ResponseEntity<Response> checkBalance(@RequestBody Account account) {
        log.info("##begin dropOne");
        Assert.notNull(account, "输入不能为空");
        String from = account.getPub();
        Long chainid = account.getChainid();
        String contractaddress = account.getContractaddress();
        String gasBalance = dropService.checkGasBalance(chainid, from);
        String tokenBalance = dropService.checkTokenBalance(chainid, from, contractaddress);
        account.setBalance(gasBalance);
        account.setContractbalance(tokenBalance);
        return success(account);
    }


    @GetMapping(value = "/chains")
    @ResponseBody
    public ResponseEntity<Response> chains() {
        log.info("##get chains");
        List<Chain> chains = dropService.chains();
        return success(chains);
    }

    @PostMapping(value = "/more/{address}")
    @ResponseBody
    public ResponseEntity<Response> more(@RequestBody DropOne dropOne) throws IOException, ExecutionException, InterruptedException {
        log.info("##airdrop address:{}", dropOne.getToaddress());

        return null;
    }


    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<Response> upload(@RequestParam("file") MultipartFile multipartFile) {
        String result = "文件格式不正确";
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            int end = originalFilename.lastIndexOf("txt");
            if (end != originalFilename.length() - 3) {
                return failure(result);
            }
            List<String> list = convertStreamToString(multipartFile.getInputStream());
            return success(list);

        } catch (IOException e) {
            e.printStackTrace();
        }
        result = "上传失败";
        return success(result);
    }


    @GetMapping("/download")
    @ResponseBody
    public void download(HttpServletResponse response) {
        String filePath = appConfig.getFilepath();
        String fileName = "demo.txt";
        UtilFile.fileDownload(response, filePath, fileName);
    }


    public static List<String> convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> list = new ArrayList<>();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return list;
    }


}
