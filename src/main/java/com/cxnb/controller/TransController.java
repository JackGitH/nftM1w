package com.cxnb.controller;

import cn.hutool.core.util.StrUtil;

import com.cxnb.config.AppConfig;
import com.cxnb.entity.Response;
import com.cxnb.enums.FunctionEnum;
import com.cxnb.exception.RequestException;
import com.cxnb.service.HecoService;
import com.cxnb.service.TransService;
import com.cxnb.service.Web3jService;
import com.cxnb.task.AirDrop;
import com.cxnb.task.Share;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.web3j.utils.Numeric;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Controller
@RequestMapping(value = "/")
@Slf4j
public class TransController extends BaseController {
    @Resource
    private HecoService hecoService;
    @Resource
    private TransService transService;
    @Resource
    private Web3jService web3jService;
    @Resource
    private Share share;
    @Resource
    AppConfig config;

    @Resource
    AirDrop drop;

    private static final String mainHome = "onb";
    private static List<String> airDrop = new ArrayList<>(); // 空投地址


/*    @RequestMapping(value = "")
    public String onbmanager(Model model) throws IOException {
        return mainHome;
    }



    @GetMapping (value = "/test")
    @ResponseBody
    public ResponseEntity<Response> test() throws IOException {
        return success("test success");
    }*/







    @PostMapping(value = "/order/airdrop/{address}")
    @ResponseBody
    public ResponseEntity<Response> airdrop(@PathVariable String address, HttpServletRequest request) throws IOException, ExecutionException, InterruptedException {
        log.info("##airdrop address:{}", address);
        boolean ethValidAddress = isETHValidAddress(address);
        String result = "";
        if (!ethValidAddress) {
            result = "无效的钱包地址";
            log.info("##无效的钱包地址:{}", address);
            throw new RequestException(result);
        }
        if(airDrop.contains(address)){
            result = address+"：已经领取过，无法重复领取";
            return failure(result);
        }
        String hash = transService.tokenDeal(web3jService.getWeb3j(), config.getPub(), address, config.getContractAddress(), config.getAirdrop(), config.getPk(), Integer.valueOf(config.getDecimal()));
        result ="领取成功,请到钱包验证，交易 "+hash;
        airDrop.add(address);
        return success(result);
    }



    @PostMapping(value = "/order/lock/{address}/{type}")
    @ResponseBody
    public ResponseEntity<Response> lock(@PathVariable String address, @PathVariable String type, HttpServletRequest request) throws IOException, ExecutionException, InterruptedException {
        log.info("##lock address:{}", address);

        String result = "fail";
        boolean ethValidAddress = isETHValidAddress(address);
        if (!ethValidAddress) {
            result = "无效的钱包地址";
            log.info("##无效的钱包地址:{}", address);
            throw new RequestException(result);
        }
        if (type.equals("1")) {
            result = hecoService.doFunc(address, FunctionEnum.lockaddress.getName());

        } else if (type.equals("2")) {
            result = hecoService.doFunc(address, FunctionEnum.removeFuncName.getName());

        } else if (type.equals("3")) {
            Boolean contains = hecoService.contains(address, FunctionEnum.containsFuncName.getName());
            if (contains == true) {
                result = "该地址已经被锁定";
                String value1 = hecoService.getValue(address, FunctionEnum.getlockvalues.getName());
                result = result + "随着时间推移，锁仓数量逐步变为" + value1;
            } else {
                result = "该地址未被锁定";
            }
        }
        return success(result);
    }


    @PostMapping(value = "/order/share/{type}")
    @ResponseBody
    public ResponseEntity<Response> share(@PathVariable String type) throws IOException, ExecutionException, InterruptedException {
        log.info("##share :{}", type);
        String result = "开始执行分红，定时分红启动成功。";
        if (type.equals("1")) {
            share.setFlage(true);
        } else if (type.equals("2")) {
            share.setFlage(false);
            result = "定时分红取消成功.";
        }
        return success(result);
    }

    @PostMapping(value = "/order/drop/{type}")
    @ResponseBody
    public ResponseEntity<Response> drop(@PathVariable String type) throws IOException, ExecutionException, InterruptedException {
        log.info("##drop :{}", type);
        String result = "开始执行空投，启动成功。";
        if (type.equals("1")) {
            drop.setFlagedrop(true);
        } else if (type.equals("2")) {
            drop.setFlagedrop(false);
            result = "取消空投成功.";
        }
        return success(result);
    }

    @PostMapping(value = "/order/findshare")
    @ResponseBody
    public ResponseEntity<Response> findshare(){
        log.info("##findshare");
        String result = "定时分红已经开启！";
        Boolean flage = share.getFlage();
        if (flage) {

        } else {
            result = "定时分红未开启！";
        }
        return success(result);
    }


    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (ip.equals("127.0.0.1")) {
                //根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }
        // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;
    }

    public static boolean isETHValidAddress(String input) {
        if (StrUtil.isEmpty(input) || !input.startsWith("0x"))
            return false;
        return isValidAddress(input);
    }

    public static boolean isValidAddress(String input) {
        String cleanInput = Numeric.cleanHexPrefix(input);

        try {
            Numeric.toBigIntNoPrefix(cleanInput);
        } catch (NumberFormatException e) {
            return false;
        }

        return cleanInput.length() == 40;
    }


    public static boolean checkEmailFormat(String content) {
        String REGEX = "^\\w+((-\\w+)|(\\.\\w+))*@\\w+(\\.\\w{2,3}){1,3}$";
        Pattern p = Pattern.compile(REGEX);
        Matcher matcher = p.matcher(content);
        return matcher.matches();
    }

    public static void main(String[] args) {
        String a = "aaa@163.com";
        System.out.println("aaa" + checkEmailFormat(a));
    }

}
