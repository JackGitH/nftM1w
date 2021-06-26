package com.cxnb.service;

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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;


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
     * 导入小号 单个
     *
     * @param pkS             私钥加密传输
     * @param contractAddress 合约地址
     */
    public Account inAccount(String pkS, Long chainId, String contractAddress) throws Exception {
        log.info("##inAccount pkS:{}", pkS);
        String result = "无效的私钥";
        if (pkS == null || pkS.equals("") || pkS.trim().length() >= pksLength) {
            log.info("##无效的私钥1:{}", pkS);
            throw new RequestException(result);
        }
        pkS= pkS.trim();
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
        pk= pk.trim();
        Credentials credentials = null;
        try {
            credentials = Credentials.create(pk);
        } catch (Exception e) {
            result = "私钥解析地址失败";
            log.error("##私钥解析地址失败:{}", pk);
            throw new RequestException(result);
        }
        String address = credentials.getAddress();


        //查询账户是否已经存在
        Account load = accountMapper.load(AccountEnum.status0.getStatus(), address);
        Account account = new Account();
        if (load != null) {
            log.info("##account already exits address:{}", address);
        } else {
            // 私钥获取地址
            log.info("##account not exits address:{}", address);
            account.setPk(pk);
            account.setPub(address);
            account.setStatus(AccountEnum.status1.getStatus());
            //DateUtil.format(new Date(),"yyyy-mm-dd hh:mm:ss");
            account.setCreatetime(new Date());
            accountMapper.add(account);
        }

        // 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);

        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));


        // 获取gas余额balance
        String balance = ethereumInfoService.getEthBalance(web3j, address);
        log.info("##balance :{}", balance);

        if (Double.valueOf(balance) < limit) {
            log.info("余额不足,余额低于0.02,请充值");
            throw new RequestException("余额不足,余额低于0.02,请充值");
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
    public PageInfo<DropOne> drop(Account account) {
        String from = account.getPk();
        String to = account.getTo();
        String amount = account.getAmount();
        String contractaddress = account.getContractaddress();
        String contractname = account.getContractname();
        Integer decimal = account.getDecimal();
        String contractbalance = account.getContractbalance();
        long chainId = account.getChainid();

        if (Double.valueOf(contractbalance) <= Double.valueOf(amount)) {
            log.warn("##token 余额不足，空投失败：{}", contractname);
            throw new RequestException(contractname + "余额不足，空投失败,余额是" + contractbalance);
        }

        // setp1 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);
        log.info("##step1 get chainInfo:{}", chain);
        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));
        // step2 获取gas余额balance 检查余额
        String balance = ethereumInfoService.getEthBalance(web3j, from);
        log.info("##step2 get balance and check balance:{}", balance);

        if (Double.valueOf(balance) < limit) {
            log.info("余额不足,余额低于0.02,请充值");
            throw new RequestException("gas余额不足,余额低于0.02,请充值");
        }

        //step3 查询账户
        Account load = accountMapper.load(AccountEnum.status0.getStatus(), from);
        log.info("##step3 检查账户存在表里");
        if (load == null) {
            log.error("##该私钥账户不存在，无法空投，请重新导入小号私钥");
            throw new RequestException("##该私钥账户不存在，无法空投，请重新导入小号私钥");
        }
        // step4 进行交易
        String hash = transService.tokenDeal(web3j, from, to, contractaddress, amount, load.getPk(), decimal);
        log.info("##step4 发起交易 hash:{}", hash);

        DropOne.DropOneBuilder builder = DropOne.builder();
        DropOne dropOne = builder.from(from)
                .to(to)
                .amount(amount)
                .chainid(chainId)
                .contractname(contractname)
                .contractaddress(contractaddress)
                .status(StatusEnum.status1.getStatus())
                .createtime(new Date())
                .hash(hash)
                .build();

        // step5 保存交易记录
        dropMapper.add(dropOne);
        log.info("##step5 drop one add table:{}", dropOne);
        // step6 返回当前账号的第一个页交易列表  默认1页 10条
        PageHelper.startPage(pageNum, pageSize);
        List<DropOne> list = dropMapper.list(StatusEnum.status0.getStatus(), null, from, contractaddress);
        log.info("##step6 drop one success and return list.size:{}", list.size());
        return new PageInfo<>(list);
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
     * 刷新空投总计
     */
    public String refreshDropTotal(String from, String contractaddress, Integer pageNum, Integer pageSize) {
        List<DropOne> listAll = dropMapper.list(StatusEnum.status0.getStatus(), null, from, contractaddress);
        List<DropOne> listSuccess = dropMapper.list(StatusEnum.status2.getStatus(), null, from, contractaddress);
        List<DropOne> listPending = dropMapper.list(StatusEnum.status1.getStatus(), null, from, contractaddress);
        List<DropOne> listFail = dropMapper.list(StatusEnum.status3.getStatus(), null, from, contractaddress);

        String result = "总计:" + listAll.size() + "条;pending:" + listPending.size() + "条;success:" + listSuccess.size() + "条;faild:" + listFail.size() + "条。";
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
     * check余额
     */
    public void checkGasBalance(long chainId, String address) {
        // 获取数据库chain 信息
        Chain chain = chainMapper.load(AccountEnum.status0.getStatus(), chainId);

        Web3j web3j = Web3j.build(new HttpService(chain.getUrl()));

        // 获取gas余额balance
        String balance = ethereumInfoService.getEthBalance(web3j, address);
        log.info("##balance :{}", balance);

        if (Double.valueOf(balance) < limit) {
            log.info("余额不足,余额低于0.02,请充值");
            throw new RequestException("余额不足,余额低于0.02,请充值");
        }
    }
}
