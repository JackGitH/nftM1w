package com.cxnb.controller;

import com.cxnb.entity.Account;
import com.cxnb.entity.DropOne;
import com.cxnb.entity.Response;
import com.cxnb.service.AirDropService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping(value = "/drop")
@Slf4j
public class AirDropController extends BaseController{
    @Resource
    AirDropService dropService;


    @PostMapping(value = "include")
    @ResponseBody
    public ResponseEntity<Response> inAccount(@RequestParam String pkS,@RequestParam Long chainId,@RequestParam String contractAddress) throws Exception {
        log.info("##begin inAccount");
        Account account = dropService.inAccount(pkS, chainId, contractAddress);

        return success(account);
    }

    @PostMapping(value = "/one/{address}")
    @ResponseBody
    public ResponseEntity<Response> drop(@RequestBody Account account) throws IOException, ExecutionException, InterruptedException {
        log.info("##begin dropOne");
        PageInfo<DropOne> dropList = dropService.drop(account);

        return success(dropList);
    }

    @PostMapping(value = "/more/{address}")
    @ResponseBody
    public ResponseEntity<Response> more(@RequestBody DropOne dropOne) throws IOException, ExecutionException, InterruptedException {
        log.info("##airdrop address:{}", dropOne.getTo());

        return null;
    }

}
