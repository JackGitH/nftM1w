package com.cxnb.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cxnb.entity.Account;
import com.cxnb.entity.Chain;
import com.cxnb.entity.DropOne;
import com.cxnb.enums.AccountEnum;
import com.cxnb.enums.StatusEnum;
import com.cxnb.exception.RequestException;
import com.cxnb.mapper.AccountMapper;
import com.cxnb.mapper.ChainMapper;
import com.cxnb.mapper.DropMapper;
import com.cxnb.util.AesEncryptUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.annotation.Resource;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;


@Service
@Slf4j
public class AirDropService {
    @Resource
    AccountMapper accountMapper;

    @Resource
    ChainMapper chainMapper;

    @Resource
    EthereumInfoService ethereumInfoService;

    @Resource
    DropMapper dropMapper;

    @Resource
    TransService transService;

    private static final Integer pkLength = 66;
    private static final Integer pksLength = 110;
    private static final Double limit = 0.02;

    private static Integer pageNum = 1;
    private static Integer pageSize = 10;


    /**
     * 导入账号 多个
     */
    public List<Account> inAccounts(List<String> pkSs, Long chainId, String contractAddress) throws Exception {
        List<Account> list = new ArrayList<>();
        Set<String> pksSet  = new HashSet<>();
        for (String pkS : pkSs) {
            // 去重
            pksSet.add(pkS);
        }
        for (String pkS : pksSet) {
            Account account = inAccount(pkS, chainId, contractAddress);
            list.add(account);
        }
        return list;
    }


    /**
     * 导入小号 单个
     *
     * @param pkS             私钥加密传输
     * @param contractAddress 合约地址
     */
    public Account inAccount(String pkS, Long chainId, String contractAddress) throws Exception {
        log.info("##inAccount pkS:{}", pkS);
        String result = "存在无效的私钥，请检查";
        if (pkS == null || pkS.equals("") || pkS.trim().length() >= pksLength) {
            log.info("##无效的私钥1:{}", pkS);
            throw new RequestException(result);
        }
        pkS = pkS.trim();
        if (contractAddress == null || contractAddress.equals("") || contractAddress.length() != 42) {
            log.info("##无效的合约地址:{}", pkS);
            result = "无效的合约地址，请检查:" + contractAddress;
            throw new RequestException(result);
        }
        String pk = AesEncryptUtil.desEncrypt(pkS);
        if (pk == null || pk.equals("") || pk.trim().length() != pkLength || !pk.startsWith("0x")) {
            log.info("##无效的私钥2:{}", pk);
            throw new RequestException(result);
        }
        pk = pk.trim();
        Credentials credentials = null;
        try {
            credentials = Credentials.create(pk);
        } catch (Exception e) {
            result = "私钥解析地址失败:" + pk;
            log.error("##私钥解析地址失败:{}", pk);
            throw new RequestException(result);
        }
        String address = credentials.getAddress();


        //查询账户是否已经存在
        Account load = accountMapper.load(AccountEnum.status0.getStatus(), address);
        Account account = new Account();
        if (load != null) {
            log.info("##account already exits address:{}", address);
            account.setPub(address);
            account.setChainid(chainId);
        } else {
            // 私钥获取地址
            log.info("##account not exits address:{}", address);
            account.setPk(pk);
            account.setChainid(chainId);
            account.setPub(address);
            account.setStatus(AccountEnum.status1.getStatus());
            //DateUtil.format(new Date(),"yyyy-mm-dd hh:mm:ss");
            account.setCreatetime(DateUtil.format(new Date(), "yyyy-MM-dd hh:mm:ss"));
            accountMapper.add(account);
        }

        // 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);

        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));


        // 获取gas余额balance
        String balance = ethereumInfoService.getEthBalance(web3j, address);
        log.info("##balance :{}", balance);

        if (Double.valueOf(balance) < limit) {
            log.info("余额不足,余额低于0.02,请充值：{}", pk);
            throw new RequestException("余额不足,余额低于0.02,请充值：" + pk);
        }


        // gas的余额和名称
        account.setBalance(balance);
        account.setTokenname(chain.getTokenname());


        //代币的余额和名称
        //获取 代币名称
        String tokenName = ethereumInfoService.getTokenName(web3j, contractAddress);
        account.setContractname(tokenName);
        account.setContractaddress(contractAddress);
        // 获取代币余额
        String erc20Balance = ethereumInfoService.getErc20Balance(web3j, contractAddress, address);
        account.setContractbalance(erc20Balance);
        // 获取代币小数位数
        int tokenDecimals = ethereumInfoService.getTokenDecimals(web3j, contractAddress);
        account.setDecimal(tokenDecimals);

        account.setPk("");// 置空私钥  防止泄密

        return account;


    }


    /**
     * 刷新数据
     *
     * @param address         地址
     * @param contractAddress 合约地址
     * @param chainId         链id  不同的链 id不同
     */
    public Account refreshAccount(String address, Long chainId, String contractAddress) {
        log.info("##refreshAccount address:{}", address);
        Account account = new Account();
        account.setPub(address);

        // 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);

        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));

        // 获取gas余额balance
        String balance = ethereumInfoService.getEthBalance(web3j, address);
        log.info("##balance :{}", balance);

        // gas的余额和名称
        account.setBalance(balance);
        account.setTokenname(chain.getTokenname());


        //代币的余额和名称
        //获取 代币名称
        String tokenName = ethereumInfoService.getTokenName(web3j, contractAddress);
        account.setContractname(tokenName);
        account.setContractaddress(contractAddress);
        // 获取代币余额
        String erc20Balance = ethereumInfoService.getErc20Balance(web3j, contractAddress, address);
        account.setContractbalance(erc20Balance);
        // 获取代币小数位数
        int tokenDecimals = ethereumInfoService.getTokenDecimals(web3j, contractAddress);
        account.setDecimal(tokenDecimals);
        account.setPk("");// 置空私钥  防止泄密

        return account;


    }


    /**
     * 空投单个
     */
    public PageInfo<DropOne> drop(Account account) throws IOException, InterruptedException {
        String fromaddress = account.getPub();
        String toaddress = account.getToaddress();
        String amount = account.getAmount();
        String contractaddress = account.getContractaddress();
        String contractname = account.getContractname();
        Integer decimal = account.getDecimal();
        long chainId = account.getChainid();


        // setp1 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);
        log.info("##step1 get chainInfo:{}", chain);
        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));
        // step2 获取gas余额balance 和 token余额 检查余额
        String balance = ethereumInfoService.getEthBalance(web3j, fromaddress);
        log.info("##step2 get balance and check balance:{}", balance);

        if (Double.valueOf(balance) < limit) {
            log.info("余额不足,余额低于0.02,请充值");
            throw new RequestException("gas余额不足,余额低于0.02,请充值");
        }

        String erc20Balance = ethereumInfoService.getErc20Balance(web3j, contractaddress, fromaddress);
        if (Double.valueOf(erc20Balance) <= Double.valueOf(amount)) {
            log.warn("##token 余额不足，空投失败：{}", contractname);
            throw new RequestException(contractname + "token余额不足，空投失败,余额是" + erc20Balance);
        }

        //step3 查询账户
        Account load = accountMapper.load(AccountEnum.status0.getStatus(), fromaddress);
        log.info("##step3 检查账户存在表里");
        if (load == null) {
            log.error("##该私钥账户不存在，无法空投，请重新导入小号私钥");
            throw new RequestException("##该私钥账户不存在，无法空投，请重新导入小号私钥");
        }
        // step4 进行交易
        String hash = transService.tokenDeal(web3j, fromaddress, toaddress, contractaddress, amount, load.getPk(), decimal);
        log.info("##step4 发起交易 hash:{}", hash);

        DropOne.DropOneBuilder builder = DropOne.builder();
        DropOne dropOne = builder.fromaddress(fromaddress)
                .toaddress(toaddress)
                .amount(amount)
                .datetime(DateUtil.format(new Date(), "yyyy-MM-dd hh:mm:ss"))
                .chainid(chainId)
                .contractname(contractname)
                .contractaddress(contractaddress)
                .txstatus(StatusEnum.status1.getStatus())
                .createtime(DateUtil.format(new Date(), "yyyy-MM-dd hh:mm:ss"))
                .txhash(hash)
                .build();

        // step5 保存交易记录
        dropMapper.add(dropOne);
        log.info("##step5 drop one add table:{}", dropOne);


        //step6 扣除手续费  token转账要和收取手续费岔开时间
        Thread.sleep(5000);
        Credentials credentials = Credentials.create(load.getPk());
        log.info("##step6 charge free txhash:{}", hash);
        charge(web3j, credentials, chain.getRecipientaddress(), chain.getRate(), chainId, chain.getPriceurl());

        // step7 返回当前账号的第一个页交易列表  默认1页 10条
        PageHelper.startPage(pageNum, pageSize);
        List<DropOne> list = dropMapper.list(StatusEnum.status0.getStatus(), null, fromaddress, contractaddress);
        log.info("##step7 drop one success and return list.size:{}", list.size());
        return new PageInfo<>(list);
    }



    /**
     * 空投批量
     */
    public void dropmore(Account account) throws IOException, InterruptedException {
        // setp1 获取数据库chain 信息
        long chainId = account.getChainid();
        String fromaddress = account.getPub();
        Account load = accountMapper.load(AccountEnum.status0.getStatus(), fromaddress);
        log.info("##step1 检查账户存在表里");
        if (load == null) {
            log.error("##该私钥账户不存在，无法空投，请重新导入小号私钥");
            throw new RequestException("##该私钥账户不存在，无法空投，请重新导入小号私钥");
        }


        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);
        log.info("##step2 get chain:{}",chain);


        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));
        Map<String,String> map = new HashMap<>();

        log.info("##step3 去除无效交易 ");
        for(String toaddressAndCount:account.getToaddressList()) {
            boolean b = toaddressAndCount.contains(",");
            if (!b) {
                log.info("##无效地址 not contains ,");
                continue;
            }
            String[] split = toaddressAndCount.split(",");
            if (split.length == 1) {
                log.info("##无效地址 length==1");
                continue;
            }
            String toaddress = split[0];
            String amount = split[1];
            if (Double.valueOf(amount) <= 0) {
                log.info("##无效数量 amount<=0");
                continue;
            }
            map.put(toaddress,amount);
        }

        log.info("##step4 计算余额是否充足 ");
        if(map.size()>=500){
            log.info("##每次最多一次性空投500个地址");
            throw new RequestException("每次最多一次性空投500个地址");
        }
        log.info("##此批空投有效数量是:{}",map.size());
        //step6 扣除手续费  token转账要和收取手续费岔开时间
        String onceRate = chain.getRate(); // 单次手续费
        String totalRate = String.valueOf(Double.valueOf(onceRate) * (map.size())); // 总计手续费
        String ethBalance = ethereumInfoService.getEthBalance(web3j, account.getPub());
        if(1.5 * Double.valueOf(totalRate) >Double.valueOf(ethBalance)){// 判断费用是否足够
            log.info("##当前余额不足以空投"+map.size()+"个有效地址，请充值后再试");
            throw new RequestException("当前余额不足以空投"+map.size()+"个有效地址，请充值后再试");
        }

        log.info("##step5 charge free：{}",totalRate);
        Credentials credentials = Credentials.create(load.getPk());
        charge(web3j, credentials, chain.getRecipientaddress(), totalRate, chainId, chain.getPriceurl());
        Thread.sleep(6000);

        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String toaddress = entry.getKey();
            String amount = entry.getValue();
            System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());

            log.info("##contractname:{} toaddress:{} amount:{}",account.getContractname(),toaddress,amount);


            String contractaddress = account.getContractaddress();
            String contractname = account.getContractname();
            Integer decimal = account.getDecimal();

            // step4 进行交易
            String hash = transService.tokenDeal(web3j, fromaddress, toaddress, contractaddress, amount, load.getPk(), decimal);
            log.info("##step6 发起交易 hash:{}", hash);

            DropOne.DropOneBuilder builder = DropOne.builder();
            DropOne dropOne = builder.fromaddress(fromaddress)
                    .toaddress(toaddress)
                    .amount(amount)
                    .datetime(DateUtil.format(new Date(), "yyyy-MM-dd hh:mm:ss"))
                    .chainid(chainId)
                    .contractname(contractname)
                    .contractaddress(contractaddress)
                    .txstatus(StatusEnum.status1.getStatus())
                    .createtime(DateUtil.format(new Date(), "yyyy-MM-dd hh:mm:ss"))
                    .txhash(hash)
                    .build();

            // step5 保存交易记录
            log.info("##step7 drop success one and add table:{}", dropOne);
            dropMapper.add(dropOne);
        }
    }


    /**
     * 刷新空投列表
     */
    public PageInfo<DropOne> refreshDropList(String from, String contractaddress, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DropOne> list = dropMapper.list(StatusEnum.status0.getStatus(), null, from, contractaddress);
        return new PageInfo<>(list);
    }

    /**
     * check gas余额
     */
    public String checkGasBalance(long chainId, String address) {
        // 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);

        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));

        // 获取gas余额balance
        String balance = ethereumInfoService.getEthBalance(web3j, address);
        log.info("##balance :{}", balance);

        return balance;
    }


    /**
     * check token额
     */
    public String checkTokenBalance(long chainId, String address, String contractaddress) {
        // 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);

        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));

        String erc20Balance = ethereumInfoService.getErc20Balance(web3j, contractaddress, address);
        return erc20Balance;
    }

    /**
     * 刷新空投总计
     */
    public String refreshDropTotal(String from, String contractaddress) {
        List<DropOne> listAll = dropMapper.list(StatusEnum.status0.getStatus(), null, from, contractaddress);
        List<DropOne> listSuccess = dropMapper.list(StatusEnum.status2.getStatus(), null, from, contractaddress);
        List<DropOne> listPending = dropMapper.list(StatusEnum.status1.getStatus(), null, from, contractaddress);
        List<DropOne> listFail = dropMapper.list(StatusEnum.status3.getStatus(), null, from, contractaddress);

        String result = "当前账号总计空投:" + listAll.size() + "条;正在交易:" + listPending.size() + "条;成功:" + listSuccess.size() + "条;失败:" + listFail.size() + "条。";
        return result;
    }


    /**
     * 翻页空投列表
     */
    public PageInfo<DropOne> getDropList(String from, String contractaddress, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DropOne> list = dropMapper.list(StatusEnum.status0.getStatus(), null, from, contractaddress);
        return new PageInfo<>(list);
    }


    /**
     * 收费
     *
     * @param recipientAddress 收款地址
     */
    public void charge(Web3j web3, Credentials credentials, String recipientAddress, String rate, Long chainId, String priceurl) throws IOException {
        log.info("##----------------------charge begin-------------------");
        log.info("##charge begin chainId:{} rate:{} address:{}", chainId, rate, credentials.getAddress());
        // Decrypt and open the wallet into a Credential object
        log.info("##charge begin Account address:{} ", credentials.getAddress());
        log.info("##charge begin Balance:{} ", Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER));
        // Get the latest nonce
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        log.info("##charge nonce: {}", nonce);

        // Value to transfer (in wei)
        BigInteger value = Convert.toWei(rate, Convert.Unit.ETHER).toBigInteger();
        log.info("##charge value:{} ", value);

        // Gas Parameters
        String s = HttpUtil.get(priceurl);
        JSONObject jsonObject = JSONObject.parseObject(s);
        String median = jsonObject.getJSONObject("prices").getString("median");
        log.info("##charge gasPrice median:{} ", median);
        BigInteger gasLimit = BigInteger.valueOf(210000);
        BigInteger gasPrice = Convert.toWei(median, Convert.Unit.GWEI).toBigInteger();
        log.info("##charge gasPrice:{} ", gasPrice);

        // Prepare the rawTransaction
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                gasPrice,
                gasLimit,
                recipientAddress,
                value);

        // Sign the transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        // Send transaction
        EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
        String transactionHash = ethSendTransaction.getTransactionHash();
        log.info("##charge transactionHash: " + transactionHash);
    }


    /**
     * 获取所有chain列表
     */
    public List<Chain> chains() {
        List<Chain> list = chainMapper.list(AccountEnum.status0.getStatus());
        return list;
    }

}
