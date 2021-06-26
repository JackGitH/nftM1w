package com.cxnb.service;

import com.cxnb.config.AppConfig;
import com.cxnb.enums.DescirptionEnum;
import com.cxnb.exception.RequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.cxnb.service.TransService.getHrc20GasLimit;

@Service
@Slf4j
public class HecoService {

    @Resource
    AppConfig config;
    @Resource
    Web3jService web3jService;

    private static final BigDecimal limit = new BigDecimal(0.009);


    /**
     * 执行lock 或者解锁
     **/
    public String doFunc(String address, String funcName) throws ExecutionException, InterruptedException, IOException {
        Web3j web3j = web3jService.getWeb3j();
        log.info("##locak address:{} funcName:{}", address, funcName);

        //setp 1  check balance
        Credentials credentials = Credentials.create(config.getPk());
        log.info("##Account address:{} " ,credentials.getAddress());
        try {
            BigDecimal balance = Convert.fromWei(web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER);
            log.info("##balance :{}", balance);
            if (balance.compareTo(limit) == -1) {
                log.info("HT余额不足,余额低于0.01,请充值");
                throw new RequestException("HT余额不足,余额低于0.01,请充值");
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.info("##getbalance err:{}", address);
            throw new RequestException("操作失败,请稍后重试:" + address);
        }


        //获取交易笔数
        BigInteger nonce;
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(config.getPub(), DefaultBlockParameterName.PENDING).send();
        if (ethGetTransactionCount == null) {
            throw new RequestException("操作失败,ethGetTransactionCount err:" + address);
        }
        nonce = ethGetTransactionCount.getTransactionCount();
        log.info("##nonce:{}", nonce);

        EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
        if (ethGasPrice == null) {
            throw new RequestException("操作失败,ethGasPrice is null:" + address);
        }

        log.info("##ethGasPrice:{}", ethGasPrice.getGasPrice());

        BigInteger gasPrice = null;
        try {
            gasPrice = TransService.getGasPrice(web3j);
            //gasPrice =BigInteger.valueOf(2000000000);
        } catch (IOException e) {
            log.info("##getGasPrice err:{}", address);
            e.printStackTrace();
        }
        log.info("##gasPrice:{}", gasPrice);
        // gasPrice =new BigInteger("0.000000001");
        //注意手续费的设置，这块很容易遇到问题
        BigInteger gasLimit = null;
        try {
            gasLimit = getHrc20GasLimit(web3j);
            gasLimit = BigInteger.valueOf(300000);
        } catch (IOException e) {
            log.info("##getHrc20GasLimit err:{}", address);
            e.printStackTrace();
            throw new RequestException("gasLimit err");
        }
        log.info("##gasLimit:{}", gasLimit);

        Function function = new Function(
                funcName,
                Arrays.asList(new Address(address)),
                Collections.singletonList(new TypeReference<Type>() {
                }));
        //创建交易对象
        String encodedFunction = FunctionEncoder.encode(function);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                config.getContractAddress(), encodedFunction);

        //进行签名操作
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signMessage);
        //发起交易
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        String hash = ethSendTransaction.getTransactionHash();

        log.info("##excute success address:{} hash:{} result:{}", address, hash, ethSendTransaction.getResult());


        return DescirptionEnum.dosucc.getMsg() + address + " hash:" + hash;
    }


    /**
     * 判断地址是否被锁定
     **/
    public Boolean contains(String address, String funcName) {
        Web3j web3j = web3jService.getWeb3j();

        //Web3j web3j = Web3j.build(new HttpService(config.getHecoInfoUrl()));

        //String hex = Integer.toHexString(Integer.valueOf(config.getPk()));

        log.info("##contains:{} funcName:{} hecoInfoUrl:{}", address,funcName,config.getHecoInfoUrl());
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address input = new Address(address);
        inputParameters.add(input);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(funcName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        log.info("##step1 pub:{} contractAddress:{}",config.getPub(),config.getContractAddress());
        Transaction transaction = Transaction.createEthCallTransaction(config.getPub(), config.getContractAddress(), data);
        log.info("##step2");
        EthCall ethCall;
        BigInteger res = BigInteger.ZERO;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            log.info("##results:{}", results);
            res = (BigInteger) results.get(0).getValue();
            log.info("##res:{}", res);
            if (res.equals(BigInteger.ZERO)) {
                log.info("##地址未被锁定: {}", address);
                return false;
            } else {
                log.info("##地址已经被锁定: {}", address);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 获取被锁定的数量
     **/
    public String getValue(String address, String funcName) {
        Web3j web3j = web3jService.getWeb3j();
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address input = new Address(address);
        inputParameters.add(input);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        outputParameters.add(typeReference);
        outputParameters.add(typeReference);
        outputParameters.add(typeReference);
        Function function = new Function(funcName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        Transaction transaction = Transaction.createEthCallTransaction(config.getPub(), config.getContractAddress(), data);
        EthCall ethCall;
        BigInteger v1 = BigInteger.ZERO;
        BigInteger v2 = BigInteger.ZERO;
        BigInteger v3 = BigInteger.ZERO;
        BigInteger v4 = BigInteger.ZERO;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());

            if (results.size() != 1) {
                v1 = (BigInteger) results.get(0).getValue();
                v2 = (BigInteger) results.get(1).getValue();
                v3 = (BigInteger) results.get(2).getValue();
                v4 = (BigInteger) results.get(3).getValue();
            }
            log.info("##{}:v1:{} v2:{} v3:{} v4:{}", function, v1, v2, v3, v4);
            double dv1 = v1.doubleValue();
            double dv2 = v2.doubleValue();
            double dv3 = v3.doubleValue();
            double dv4 = v4.doubleValue();
            Integer decimal  = Integer.valueOf(config.getDecimal());
            log.info("##{}:dv1:{} dv2:{} dv3:{} dv4:{}", function, dv1 / (Math.pow(10.00, decimal)), dv2 / (Math.pow(10.00, decimal)), dv3 / (Math.pow(10.00, decimal)), dv4 / (Math.pow(10.00, decimal)));
            return dv1 / (Math.pow(10.00, decimal)) + "-" + dv2 / (Math.pow(10.00, decimal)) + "-" +  dv3 / (Math.pow(10.00, decimal)) + "-" + dv4 / (Math.pow(10.00, decimal));
        } catch (IOException e) {
            e.printStackTrace();
            return "0-0-0-0";
        }
    }




    /**
     * 调用其他不花费gas的方法
     **/
    public BigInteger greyFucn(String address, String funcName) {
        Web3j web3j = web3jService.getWeb3j();

        //Web3j web3j = Web3j.build(new HttpService(config.getHecoInfoUrl()));

        //String hex = Integer.toHexString(Integer.valueOf(config.getPk()));

        log.info("##greyFucn:{} funcName:{} hecoInfoUrl:{}", address,funcName,config.getHecoInfoUrl());
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address input = new Address(address);
        inputParameters.add(input);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(funcName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        log.info("##step1 pub:{} contractAddress:{}",config.getPub(),config.getContractAddress());
        Transaction transaction = Transaction.createEthCallTransaction(config.getPub(), config.getContractAddress(), data);
        log.info("##step2");
        EthCall ethCall;
        BigInteger res = BigInteger.ZERO;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            log.info("##results:{}", results);
            res = (BigInteger) results.get(0).getValue();
            log.info("##res:{}", res);
            return res;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    public static void main(String[] args) throws IOException {
        String path = "D:\\ajava\\coin\\cxnb\\src\\main\\java\\com\\cxnb\\service\\tokenAddress.txt";
        List<String> list = readFile02(path);
        for(String s :list){
            System.out.println("s:"+s);
        }
    }


    /**
     * 批量锁
     **/
    public  void lockAll(String address) throws ExecutionException, InterruptedException, IOException {
        Web3j web3j = web3jService.getWeb3j();
        log.info("##locak address:{} funcName:{}", address, "lockaddress");
        Thread.sleep(300);

        //setp 1  check balance
        Credentials credentials = Credentials.create(config.getPk());
        System.out.println("##Account address: " + credentials.getAddress());
        try {
            BigDecimal balance = Convert.fromWei(web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER);
            log.info("##balance :{}", balance);
            if (balance.compareTo(limit) == -1) {
                log.info("##HT余额不足,余额低于0.01,请充值:{}", address);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.info("##getbalance err:{}", address);
            log.info("操作失败,请稍后重试:{}", address);
        }
    }


    /**读文件*/
    public static List readFile02(String path) throws IOException {
        // 使用一个字符串集合来存储文本中的路径 ，也可用String []数组

        List list = new ArrayList();
        FileInputStream fis = new FileInputStream(path);
        // 防止路径乱码 如果utf-8 乱码 改GBK eclipse里创建的txt 用UTF-8，在电脑上自己创建的txt 用GBK
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line = "";
        while ((line = br.readLine()) != null) {
            if (line.lastIndexOf("---") < 0) {
                list.add(line);
            }
        }
        br.close();
        isr.close();
        fis.close();
        return list;
    }


}
