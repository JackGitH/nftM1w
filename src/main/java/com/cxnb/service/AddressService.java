package com.cxnb.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cxnb.config.AppConfig;
import com.cxnb.entity.TransInfo;
import com.cxnb.entity.TransRes;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AddressService {
    @Resource
    AppConfig config;

    private static final String success = "1";
    private static final String finish = "0";

    /**查看最有价值账户地址*/
    public List<String> topAccounts(){
        List<String> htmlAddressList  = new ArrayList<>();
        for(int i =1;i<101;i++){
            String html = HttpUtil.get("https://hecoinfo.com/accounts/"+i+"?ps=100");
            // String html = HttpUtil.get("https://hecoinfo.com/token/generic-tokentxns2?contractAddress=0xa71edc38d189767582c38a3145b5873052c3e47a&mode=&sid=b1efecca3216648a8e4b0a31615d0191&m=normal&p=4");
            Document doc = Jsoup.parse(html);
            List<String> list = match(html.toString(), "a", "href");
            log.info("##list:{}",list.size());
            for(String str:list){
                boolean isStart = str.startsWith("/address/");
                if(isStart){
                    int begin = str.indexOf("0x");
                    int end = str.indexOf("'>");
                    String strEnd = str.substring(begin, end);
                    htmlAddressList.add(strEnd);
                }
            }
            log.info("##htmlAddressList:{}",htmlAddressList);
        }
        return htmlAddressList;
    }

    /**获取usdt 在内的排名前二十的token持有账户*/
    public List<String> tokenAccounts(){
        boolean totalFlag = true;
        String apiKey = config.getApikey();

        List<String> addRessList  = new ArrayList<>();
        addRessList.add("0xa71edc38d189767582c38a3145b5873052c3e47a"); // usdt
        addRessList.add("0xD2e860D923c997764b229f6F397CC2Cc5d8c9aFf"); // ONB
        addRessList.add("0x64ff637fb478863b7468bc97d30a5bf3a428a1fd"); // ETH
        addRessList.add("0x70d171d269d964d14af9617858540061e7be9ef1"); // WBTC
        addRessList.add("0xef3cebd77e0c52cb6f60875d9306397b5caca375"); // HBCH
        addRessList.add("0x66a79d23e58475d2738179ca52cd0b41d73f0bea"); // hbtc
        addRessList.add("0x9362bbef4b8313a8aa9f0c9808b80577aa26b73b"); // HECO
        addRessList.add("0xa2c49cee16a5e5bdefde931107dc1fae9f7773e3"); // hdot
        addRessList.add("0x22c54ce8321a4015740ee1109d9cbc25815c46e6"); // UNI
        addRessList.add("0xecb56cf772b5c9a6907fb7d32387da2fcbfb63b4"); // hltc
        addRessList.add("0x9e004545c59d359f6b7bfb06a26390b087717b42"); // LINK
        addRessList.add("0x70d171d269d964d14af9617858540061e7be9ef1"); // wbtc
        addRessList.add("0x3d760a45d0887dfd89a2f5385a236b29cb46ed2a"); // DAI-HECO
        addRessList.add("0x5545153ccfca01fbd7dd11c0b23ba694d9509a6f"); // wht
        addRessList.add("0x45e97dad828ad735af1df0473fc2735f0fd5330c"); // hxtz
        addRessList.add("0x1c9491865a1de77c5b6e19d2e6a5f1d7a6f2b25f"); // MATTER
        addRessList.add("0x2aafe3c9118db36a20dd4a942b6ff3e78981dce1"); // GOF
        addRessList.add("0xe499ef4616993730ced0f31fa2703b92b50bb536"); // hpt


        Set<String> addrSet = new HashSet<>();

        Integer pageHold= 1;
        Integer offset = 500;

        for(String add: addRessList){
            totalFlag = true;
            Integer page = 1;
            while (totalFlag) {
                if (page >= 20) {
                    break;
                }
                String url = "https://api.hecoinfo.com/api?module=account&action=tokentx&contractaddress=" + add + "&page=" + page + "&offset=" + offset + "&sort=asc&apikey=" + apiKey;//指定URL
                //发送get请求并接收响应数据
                String result = HttpUtil.createGet(url).execute().body();
                TransRes transRes = JSONObject.parseObject(result, TransRes.class);
                if (success.equals(transRes.getStatus())) {
                    for (TransInfo e : transRes.getResult()) {
                        addrSet.add(e.getTo());
                    }
                    log.info("##合约address:{} page:{} 地址数量:{}",add,page,addrSet.size());
                    page++;
                } else if (finish.equals(transRes.getStatus())) {
                    log.info("##调用接口查询数据完成 url:{}", url);
                    log.info("##合约address:{} page:{} 不重复的地址数量:{}", add,page,addrSet.size());
                    totalFlag = false;
                } else {
                    log.info("##调用接口查询数据完成 url:{}", url);
                    log.error("##调用接口查询失败 url:{}", url);
                    log.info("##合约address:{} 不重复的交易列表:{}", add,addrSet.size());
                    totalFlag = false;
                }
            }
        }
        log.info("## final addrSet.size:{}",addrSet.size());
        List<String> list = new ArrayList<>(addrSet);
        return list;
    }

    /**静态方法 解析网页*/
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
