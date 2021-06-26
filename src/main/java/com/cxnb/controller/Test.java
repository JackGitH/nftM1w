package com.cxnb.controller;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cxnb.entity.EthTransactionReceiptResult;
import com.cxnb.entity.EthTransactionResult;
import com.cxnb.entity.TransRes;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Web3j  web3j = Web3j.build(new HttpService("https://http-mainnet-node.huobichain.com"));
        Long begin  = System.currentTimeMillis();
        EthTransactionReceiptResult ethTransaction = getEthTransactionReceipt(web3j, "0x6be4c9b77b6323faad9b826e0941dcf7a2f60c5e99ee6b0ae03ecff5ae13f634");
        System.out.println("ethTransaction"+ethTransaction);
        System.out.println("time:"+(System.currentTimeMillis() - begin));


    }

    public static  EthTransactionResult getEthTransaction(Web3j web3j, String txHash) throws ExecutionException, InterruptedException {
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


    public static EthTransactionReceiptResult getEthTransactionReceipt(Web3j web3j, String txHash) throws ExecutionException, InterruptedException {
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
