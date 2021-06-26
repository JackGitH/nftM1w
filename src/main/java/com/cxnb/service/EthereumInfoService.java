package com.cxnb.service;

import com.cxnb.entity.EthTransactionReceiptResult;
import com.cxnb.entity.EthTransactionResult;
import com.cxnb.enums.FunctionEnum;
import com.cxnb.exception.RequestException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * The type Ethereum info service.
 */
@Service
@Scope("singleton")
@Slf4j
public class EthereumInfoService {


    private static String emptyAddress = "0x0000000000000000000000000000000000000000";

    /**
     * get eth balance
     *
     * @param web3j   the web 3 j
     * @param address the address
     * @return eth balance
     */
    public String getEthBalance(Web3j web3j, String address) {
        try {
            BigDecimal balance = Convert.fromWei(web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER);
            return balance.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RequestException("获取余额失败" + ex.getMessage());
        }
    }


    /**
     * get erc20 balance
     *
     * @param web3j    the web 3 j
     * @param contract the contract
     * @param address  the address
     * @return erc 20 balance 去除decimal之后的剩下的数
     */
    public String getErc20Balance(Web3j web3j, String contract, String address) {
        try {
            String funcName = FunctionEnum.balanceOf.getName();
            log.info("##greyFucn:{} funcName:{}", address, funcName);
            List<Type> inputParameters = new ArrayList<>();
            List<TypeReference<?>> outputParameters = new ArrayList<>();
            Address input = new Address(address);
            inputParameters.add(input);

            TypeReference<Uint256> typeReference = new TypeReference<Uint256>() {
            };
            outputParameters.add(typeReference);
            Function function = new Function(funcName, inputParameters, outputParameters);
            String data = FunctionEncoder.encode(function);
            log.info("##step1 pub:{} contractAddress:{}", address, contract);
            org.web3j.protocol.core.methods.request.Transaction transaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(address, contract, data);
            log.info("##step2");
            EthCall ethCall;
            BigInteger res = BigInteger.ZERO;
            try {
                ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).send();
                List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
                log.info("##results:{}", results);
                res = (BigInteger) results.get(0).getValue();
                log.info("##res:{}", res);
                double dvBalance = res.doubleValue();

                int tokenDecimals = getTokenDecimals(web3j, contract);
                log.info("##contract:{} tokenDecimals:{}",contract,tokenDecimals);


                double pow =dvBalance / (Math.pow(10.00, tokenDecimals));
                BigDecimal bg=new BigDecimal(pow+"");

                return bg.toString();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("##获取代币余额失败:{}",contract);
            throw new RequestException("获取代币余额失败"+ex.getMessage());
        }
    }



    /**
     * 查询代币精度
     *
     * @param web3j
     * @param contractAddress
     * @return
     */
    public  int getTokenDecimals(Web3j web3j, String contractAddress) {
        String methodName = FunctionEnum.decimals.getName();
        String fromAddr = emptyAddress;
        int decimal = 0;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Uint8> typeReference = new TypeReference<Uint8>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.request.Transaction transaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            decimal = Integer.parseInt(results.get(0).getValue().toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return decimal;
    }


    /**
     * 查询代币名称
     *
     * @param web3j
     * @param contractAddress
     * @return
     */
    public  String getTokenName(Web3j web3j, String contractAddress) {
        String methodName = FunctionEnum.names.getName();
        String name = null;
        String fromAddr = emptyAddress;
        List<Type> inputParameters = new ArrayList<>();
        List<TypeReference<?>> outputParameters = new ArrayList<>();

        TypeReference<Utf8String> typeReference = new TypeReference<Utf8String>() {
        };
        outputParameters.add(typeReference);

        Function function = new Function(methodName, inputParameters, outputParameters);

        String data = FunctionEncoder.encode(function);
        org.web3j.protocol.core.methods.request.Transaction transaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(fromAddr, contractAddress, data);

        EthCall ethCall;
        try {
            ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
            List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
            name = results.get(0).getValue().toString();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return name;
    }


    /**
     * Gets eth transaction.
     *
     * @param txHash the tx hash
     * @return the eth transaction
     * @throws ExecutionException   the execution exception
     * @throws InterruptedException the interrupted exception
     */
    public EthTransactionResult getEthTransaction(Web3j web3j,String txHash) throws ExecutionException, InterruptedException {
        EthTransactionResult.EthTransactionResultBuilder builder = EthTransactionResult.builder();
        if (txHash != null) {
            Transaction tx = web3j.ethGetTransactionByHash(txHash).sendAsync().get().getTransaction().orElse(null);
            if (tx != null) {
                builder.blockHash(tx.getBlockHash())
                        .blockNumber(tx.getBlockNumberRaw())
                        .from(tx.getFrom())
                        .gas(tx.getGasRaw())
                        .gasPrice(tx.getGasPriceRaw())
                        .hash(tx.getHash())
                        .input(tx.getInput())
                        .nonce(tx.getNonceRaw())
                        .to(tx.getTo())
                        .transactionIndex(tx.getTransactionIndexRaw())
                        .value(tx.getValueRaw())
                        .v(tx.getV())
                        .s(tx.getS())
                        .r(tx.getR());

                if (tx.getCreates() != null) builder.creates(tx.getCreates());
                if (tx.getChainId() != null) builder.chainId(tx.getChainId());
                if (tx.getPublicKey() != null) builder.publicKey(tx.getPublicKey());
            }
        }

        return builder.build();
    }

    /**
     * Gets eth transaction receipt.
     *
     * @param txHash the tx hash
     * @return the eth transaction receipt
     * @throws ExecutionException   the execution exception
     * @throws InterruptedException the interrupted exception
     */
    public EthTransactionReceiptResult getEthTransactionReceipt(Web3j web3j,String txHash) throws ExecutionException, InterruptedException {
        EthTransactionReceiptResult.EthTransactionReceiptResultBuilder builder = EthTransactionReceiptResult.builder();
        if (txHash != null) {
            TransactionReceipt tx = web3j.ethGetTransactionReceipt(txHash).sendAsync().get().getTransactionReceipt().orElse(null);
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

}
