package com.application.ewallet.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.protocol.types.Field;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WalletRepository walletRepository;


    @KafkaListener(topics = "create_wallet", groupId = "test1234")
    public void createWallet(String username){

        Wallet wallet = Wallet.builder().username(username).balance(100).build();
        walletRepository.save(wallet);

    }

    @KafkaListener(topics = "update-wallet", groupId = "test1234")
    public void updateWallet(String message) throws JsonProcessingException {

        // convert message to json Object
        JSONObject jsonObject = objectMapper.readValue(message, JSONObject.class);

        String fromUser = (String) jsonObject.get("fromUser");
        String toUser = (String) jsonObject.get("toUser");
        int transactionAmount = (Integer) jsonObject.get("amount");
        String transactionId = (String) jsonObject.get("transactionId");

        // create return object
        JSONObject returnObject = new JSONObject();

        returnObject.put("transactionId", transactionId);

        Wallet fromUserWallet = walletRepository.findWalletByUsername(fromUser);
        Wallet receiverWallet = walletRepository.findWalletByUsername(toUser);

        if (fromUserWallet.getBalance() >= transactionAmount) {

            // transaction can be done and update wallets
            fromUserWallet.setBalance(fromUserWallet.getBalance() - transactionAmount);
            walletRepository.save(fromUserWallet);

            receiverWallet.setBalance(receiverWallet.getBalance() + transactionAmount);
            walletRepository.save(receiverWallet);

            //That is a succesfull transaction
            returnObject.put("status", "SUCCESS");

            // to transaction service
            kafkaTemplate.send("update_transaction", objectMapper.writeValueAsString(returnObject));


        } else {

            //INSUFFICIENT BALANCE --->
            returnObject.put("status", "FAILED");
            kafkaTemplate.send("update_transaction", objectMapper.writeValueAsString(returnObject));

        }


    }

}
