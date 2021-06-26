package com.cxnb.service;

import com.cxnb.entity.EthTransactionReceiptResult;
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
import org.web3j.ens.Contracts;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;

import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Slf4j
@Service
public class TransService {
    private static String fromAddress1 = "0x9fA31Cd0886198Ea957E693C6824ca3e17d913dB";
    private static String fromAddress2 = "0x735d997e7f4ed2e5ae564647d777a41d666a3c27";

    private static String EnvironmentUrl = "https://http-mainnet-node.huobichain.com";

    private static String contractAddress = "0x4214c540152161ca47fd3341a731bda337951d21";

    private static Admin admin;

    private static String password = "!";
    private static String toAddress = "";
    private static String pk = ""; // Add a private key here

    static final String DEFAULT_CONTRACT_GASLIMIT = "91837";


    public static void main(String[] args) throws IOException {
        Web3j web3j = Web3j.build(new HttpService(EnvironmentUrl));
        admin = Admin.build(new HttpService(EnvironmentUrl));
       /* log.info("##balanceToken:{}", getTokenBalance(web3j, fromAddress2, contractAddress));

        Credentials credentials = Credentials.create(pk);
        log.info("##credentials.address:{}",credentials.getAddress());



        EthGetBalance ethGetBalance = web3j.ethGetBalance(fromAddress2, DefaultBlockParameterName.LATEST).send();
        log.info("##ethGetBalance:{}",ethGetBalance.getBalance());

        BigDecimal balanceEth = Convert.fromWei(web3j.ethGetBalance(fromAddress2, DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER);
        log.info("##balacnEth:{}",balanceEth);*/
        /*for (int i=1;i<=30;i++){
            String value  = String.valueOf(i);
            String txHash = tokenDeal(web3j, fromAddress2, toAddress, contractAddress, value,pk,9);
            log.info("##txHash:{}", txHash);
        }*/
    }

    public static BigInteger getTokenBalance(Web3j web3j, String fromAddress, String contractAddress) {

        log.info("##getTokenBalance");
        String methodName = "balanceOf";
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Address address = new Address(fromAddress);
        inputParameters.add(address);

        TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
        };
        outputParameters.add(typeReference);
        Function function = new Function(methodName, inputParameters, outputParameters);
        String data = FunctionEncoder.encode(function);
        log.info("##step1");
        Transaction transaction = Transaction.createEthCallTransaction(fromAddress, contractAddress, data);
        log.info("##step2");
        EthCall ethCall;
        BigInteger balanceValue = BigInteger.ZERO;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            log.info("##results:{}", results);
            balanceValue = (BigInteger) results.get(0).getValue();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return balanceValue;
    }


    /**
     * 代币转账
     */
    public  String tokenDeal(Web3j web3j, String from, String to, String contractAddress,String value, String privateKey , int decimal) {
        try {
            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //获取交易笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send();
            if (ethGetTransactionCount == null) {
                return null;
            }
            nonce = ethGetTransactionCount.getTransactionCount();
            log.info("##nonce:{}",nonce);

            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            if (ethGasPrice == null) {
                return null;
            }

            log.info("##ethGasPrice:{}",ethGasPrice.getGasPrice());

            BigInteger gasPrice = getGasPrice(web3j);
            log.info("##gasPrice:{}",gasPrice);
           // gasPrice =new BigInteger("0.000000001");
            //注意手续费的设置，这块很容易遇到问题
            BigInteger gasLimit = getHrc20GasLimit(web3j);
            gasLimit = BigInteger.valueOf(300000);
            log.info("##gasLimit:{}",gasLimit);


            BigInteger val = new BigDecimal(value).multiply(new BigDecimal("10").pow(decimal)).toBigInteger();// 单位换算
            //BigInteger val = new BigDecimal(value).toBigInteger();// 单位换算
            log.info("##val:{}",val);
            Function function = new Function(
                    "transfer",
                    Arrays.asList(new Address(to), new Uint256(val)),
                    Collections.singletonList(new TypeReference<Type>() {
                    }));
            //创建交易对象
            String encodedFunction = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                    contractAddress, encodedFunction);

            //进行签名操作
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signMessage);
            //发起交易
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String hash = ethSendTransaction.getTransactionHash();
            //EthTransactionReceiptResult hecoTransactionReceipt = getHecoTransactionReceipt(web3j, hash);
           // log.info("##hecoTransactionReceipt:{}",hecoTransactionReceipt);
            log.info("##txhash:{}",hash);
            return hash;

        }catch (Exception e){
            e.printStackTrace();
            throw  new RequestException("领取失败,请重试");
        }
    }

    /**
     * 代币转账的静态方法  供util使用
     * @return
     */
    public  static String tokenDealStatic(Web3j web3j, String from, String to, String contractAddress,String value, String privateKey , int decimal) {
        try {
            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //获取交易笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send();
            if (ethGetTransactionCount == null) {
                return null;
            }
            nonce = ethGetTransactionCount.getTransactionCount();
            log.info("##nonce:{}",nonce);

            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            if (ethGasPrice == null) {
                return null;
            }

            log.info("##ethGasPrice:{}",ethGasPrice.getGasPrice());

            BigInteger gasPrice = getGasPrice(web3j);
            log.info("##gasPrice:{}",gasPrice);
            // gasPrice =new BigInteger("0.000000001");
            //注意手续费的设置，这块很容易遇到问题
            BigInteger gasLimit = getHrc20GasLimit(web3j);
            gasLimit = BigInteger.valueOf(300000);
            log.info("##gasLimit:{}",gasLimit);


            BigInteger val = new BigDecimal(value).multiply(new BigDecimal("10").pow(decimal)).toBigInteger();// 单位换算
            //BigInteger val = new BigDecimal(value).toBigInteger();// 单位换算
            log.info("##val:{}",val);
            Function function = new Function(
                    "transfer",
                    Arrays.asList(new Address(to), new Uint256(val)),
                    Collections.singletonList(new TypeReference<Type>() {
                    }));
            //创建交易对象
            String encodedFunction = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                    contractAddress, encodedFunction);

            //进行签名操作
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signMessage);
            //发起交易
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String hash = ethSendTransaction.getTransactionHash();
            //EthTransactionReceiptResult hecoTransactionReceipt = getHecoTransactionReceipt(web3j, hash);
            // log.info("##hecoTransactionReceipt:{}",hecoTransactionReceipt);
            log.info("##txhash:{}",hash);
            return hash;

        }catch (Exception e){
            e.printStackTrace();
            //throw  new RequestException("领取失败,请重试");
            return  null;
        }
    }


    /**
     * 代币转账补偿  txhash为null时 代表失败 自动补偿一次
     */
    public  String tokenDealSecond(Web3j web3j, String from, String to, String contractAddress,String value, String privateKey , int decimal) {
        try {
            //转账的凭证，需要传入私钥
            Credentials credentials = Credentials.create(privateKey);
            //获取交易笔数
            BigInteger nonce;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send();
            if (ethGetTransactionCount == null) {
                return null;
            }
            nonce = ethGetTransactionCount.getTransactionCount();
            log.info("##nonce:{}",nonce);

            EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
            if (ethGasPrice == null) {
                return null;
            }

            log.info("##ethGasPrice:{}",ethGasPrice.getGasPrice());

            BigInteger gasPrice = getGasPrice(web3j);
            log.info("##gasPrice:{}",gasPrice);
            // gasPrice =new BigInteger("0.000000001");
            //注意手续费的设置，这块很容易遇到问题
            BigInteger gasLimit = getHrc20GasLimit(web3j);
            gasLimit = BigInteger.valueOf(300000);
            log.info("##gasLimit:{}",gasLimit);


            BigInteger val = new BigDecimal(value).multiply(new BigDecimal("10").pow(decimal)).toBigInteger();// 单位换算
            //BigInteger val = new BigDecimal(value).toBigInteger();// 单位换算
            log.info("##val:{}",val);
            Function function = new Function(
                    "transfer",
                    Arrays.asList(new Address(to), new Uint256(val)),
                    Collections.singletonList(new TypeReference<Type>() {
                    }));
            //创建交易对象
            String encodedFunction = FunctionEncoder.encode(function);
            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                    contractAddress, encodedFunction);

            //进行签名操作
            byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signMessage);
            //发起交易
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
            String hash = ethSendTransaction.getTransactionHash();
            //EthTransactionReceiptResult hecoTransactionReceipt = getHecoTransactionReceipt(web3j, hash);
            // log.info("##hecoTransactionReceipt:{}",hecoTransactionReceipt);
            log.info("##txhash:{}",hash);
            return hash;

        }catch (Exception e){
            e.printStackTrace();
            throw  new RequestException("领取失败,请重试");
        }
    }


    /**
     * Gets gas price.
     *
     * @param web3j the web 3 j
     * @return the gas price
     * @throws IOException the io exception
     */
    public static BigInteger getGasPrice(Web3j web3j) throws IOException {
        return web3j.ethGasPrice().send().getGasPrice();
    }


    /**
     * Gets hrc 20 gas limit.
     *
     * @param web3j the web 3 j
     * @return the hrc 20 gas limit
     * @throws IOException the io exception
     */
    public static  BigInteger getHrc20GasLimit(Web3j web3j) throws IOException {
        return new BigInteger(DEFAULT_CONTRACT_GASLIMIT);
    }


    /**
     * get heco transaction receipt by hash
     *
     * @param txHash the tx hash
     * @return heco transaction receipt
     * @throws ExecutionException   the execution exception
     * @throws InterruptedException the interrupted exception
     */
    public static EthTransactionReceiptResult getHecoTransactionReceipt(Web3j web3j, String txHash) throws ExecutionException, InterruptedException {
        EthTransactionReceiptResult.EthTransactionReceiptResultBuilder builder = EthTransactionReceiptResult.builder();
        if (txHash != null) {
            TransactionReceipt tx = getHecoTransactionReceipt2(web3j,txHash);
            if (tx != null) {
                builder.transactionHash(tx.getTransactionHash())
                        .transactionIndex(tx.getTransactionIndex())
                        .blockHash(tx.getBlockHash())
                        .blockNumber(tx.getBlockNumberRaw())
                        .from(tx.getFrom())
                        .to(tx.getTo())
                        .cumulativeGasUsed(tx.getCumulativeGasUsed())
                        .gasUsed(tx.getGasUsed())
                        .contractAddress(tx.getContractAddress())
                        .logs(tx.getLogs())
                        .logsBloom(tx.getLogsBloom())
                        .root(tx.getRoot())
                        .status(tx.getStatus());

                builder.revertReason(tx.getRevertReason());
                builder.isStatus(tx.isStatusOK());
            }
        }

        return builder.build();
    }

    /**
     * Gets heco transaction receipt.
     *
     * @param txHash the tx hash
     * @return the heco transaction receipt
     * @throws ExecutionException   the execution exception
     * @throws InterruptedException the interrupted exception
     */
    public static TransactionReceipt getHecoTransactionReceipt2(Web3j web3j,String txHash) throws ExecutionException, InterruptedException {
        return web3j.ethGetTransactionReceipt(txHash).sendAsync().get().getTransactionReceipt().orElse(null);
    }



}
