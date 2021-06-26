package com.cxnb.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cxnb.entity.TransInfo;
import com.cxnb.entity.TransRes;
import com.cxnb.service.TransService;
import jnr.ffi.annotations.In;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilTool {

    private static final String success = "1";
    private static final String finish = "0";
    private final static Logger log = LoggerFactory.getLogger(UtilTool.class);

    public static void main(String[] args) throws  Exception{
        String path1 = "D:\\ajava\\coin\\cxnb-new\\src\\main\\resources\\tokenAddress.txt";
        String path2 = "D:\\ajava\\coin\\cxnb-new\\src\\main\\resources\\topAccountAddress.txt";
        String path3 = "D:\\ajava\\coin\\cxnb-new\\src\\main\\resources\\success.txt";
        List<String> list = FileUtil.readLines(path1, "");
        List<String> list2 = FileUtil.readLines(path2, "");
        List<String> list3 = FileUtil.readLines(path3, "");
        list.addAll(list2);
        Set<String> twoAdd = new HashSet<>(list);
        Set<String> finalAdd = new HashSet<>();
        Set<String> setList3 = new HashSet<>(list3);

        String contractAddr = "0x66dD43C03fa91aBeCb8F4EE2F7F34de9ed88eB26";
        Integer page = 1;
        Integer offset = 500;
        String apiKey = "XDPZWVYIT4W21YY3DZNWBUUDQB1X6S55MY";

       /* boolean holdFlag = true;
        Integer pageHold = 1;
        Set<String> holdAddrSet = new HashSet<>();
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
        FileUtil.appendLines(holdAddrSet,path3,"");


        // 查看mob 不包含的地址
        for (String addr : twoAdd) {
            if (!holdAddrSet.contains(addr)) {
                finalAdd.add(addr);
            }
        }
        log.info("##finalAdd size1:{}",finalAdd.size());*/
        // 再次排除 mob 不包含的地址
        //Iterator<String> iterator = finalAdd.iterator();
        Iterator<String> iterator = twoAdd.iterator();
        while (iterator.hasNext()){
            String addr  =iterator.next();
            if(setList3!=null && setList3.contains(addr)){
                iterator.remove();
            }
        }

        log.info("##finalAdd size:{}",twoAdd.size());

        Web3j web3j = Web3j.build(new HttpService(" https://http-mainnet-node.huobichain.com"));
        String from = "0x735d997E7F4ED2E5Ae564647d777A41d666a3c27";
        String contractAddress  = "0x66dD43C03fa91aBeCb8F4EE2F7F34de9ed88eB26";
        String privateKey  = "0x2a0fcdde6f254758ce1e0bfd2ca36ec8ec3ee6b770d095723f82e98bf292120f";
        int decimal = 9;

        for (String addr : twoAdd) {
            Integer min = 100;
            Integer max = 500;
            Random random = new Random();
            int scale = random.nextInt(max) % (max - min + 1) + min;
            String scaleStr= String.valueOf(scale);
            String hash = TransService.tokenDealStatic(web3j, from, addr, contractAddress, scaleStr, privateKey, decimal);
            if(null == hash){
                log.info("##hash is null");
            }else {
                log.info("##hash is success:{}",hash);
                List<String> line = new ArrayList<>();
                line.add(addr);
                FileUtil.appendLines(line,path3,"");
            }
        }
    }

    public static List<String> match(String source, String element, String attr) {
        List<String> result = new ArrayList<String>();
        String reg = "<" + element + "[^<>]*?\\s" + attr + "=['\"]?(.*?)['\"]?\\s.*?>";
        Matcher m = Pattern.compile(reg).matcher(source);
        while (m.find()) {
            String r = m.group(1);
            result.add(r);
        }
        return result;
    }
}
