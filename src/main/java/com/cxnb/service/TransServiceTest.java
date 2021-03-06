package com.cxnb.service;

import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

@Service
public class TransServiceTest {
    private static String HecoUrl = "https://http-mainnet.hecochain.com";
    private static String EthUrl = "https://rinkeby.infura.io/v3/083836b2784f48e19e03487eb3209923";

    public static void main(String[] args) {
        System.out.println("Connecting to Ethereum ...");
        Web3j web3 = Web3j.build(new HttpService(HecoUrl));
        System.out.println("Successfuly connected to Ethereum");

        try {
            //String pk = "2eef9765f6aad665116f5f9c43191434cff1c0c955f341a0ad92dcc671fb7b0d"; // Add a private key here
            String pk = "2a0fcdde6f254758ce1e0bfd2ca36ec8ec3ee6b770d095723f82e98bf292120f"; // Add a private key here

            // Decrypt and open the wallet into a Credential object
            Credentials credentials = Credentials.create(pk);
            System.out.println("Account address: " + credentials.getAddress());
            System.out.println("Balance: " + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER));

           /* // Get the latest nonce
            EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
            BigInteger nonce =  ethGetTransactionCount.getTransactionCount();

            // Recipient address
            String recipientAddress = "0xAA6325C45aE6fAbD028D19fa1539663Df14813a8";

            // Value to transfer (in wei)
            BigInteger value = Convert.toWei("1", Convert.Unit.ETHER).toBigInteger();

            // Gas Parameters
            BigInteger gasLimit = BigInteger.valueOf(21000);
            BigInteger gasPrice = Convert.toWei("1", Convert.Unit.GWEI).toBigInteger();

            // Prepare the rawTransaction
            RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                    nonce,
                    gasPrice,
                    gasLimit,
                    recipientAddress,
                    value);

            // Sign the transaction
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);*/

            // Send transaction
            /*EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(hexValue).send();
            String transactionHash = ethSendTransaction.getTransactionHash();
            System.out.println("transactionHash: " + transactionHash);

            // Wait for transaction to be mined
            Optional transactionReceipt = null;
            do {
                System.out.println("checking if transaction " + transactionHash + " is mined....");
                EthGetTransactionReceipt ethGetTransactionReceiptResp = web3.ethGetTransactionReceipt(transactionHash).send();
                transactionReceipt = ethGetTransactionReceiptResp.getTransactionReceipt();
                Thread.sleep(3000); // Wait 3 sec
            } while(!transactionReceipt.isPresent());

          //  System.out.println("Transaction " + transactionHash + " was mined in block # " + transactionReceipt.get().getBlockNumber());
            System.out.println("Transaction " + transactionHash + " was mined in block # " + transactionReceipt.get());
            System.out.println("Balance: " + Convert.fromWei(web3.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance().toString(), Convert.Unit.ETHER));

*/
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
