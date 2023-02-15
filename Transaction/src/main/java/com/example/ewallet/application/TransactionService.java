package com.example.ewallet.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.simple.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.Date;
import java.util.UUID;
import java.net.URI;

@Service
public class TransactionService {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Autowired
    RestTemplate restTemplate;

    public void createTransaction(TransactionRequestDto transactionRequestDto) throws JsonProcessingException {

        try{

            // creating transaction object
            Transaction transaction = Transaction.builder().fromUser(transactionRequestDto.getFromUser()).toUser(transactionRequestDto.getToUser())
                    .amount(transactionRequestDto.getAmount()).purpose(transactionRequestDto.getPurpose()).transactionId(UUID.randomUUID().toString()).transactionDate(new Date())
                    .transactionStatus(TransactionStatus.PENDING).build();

            transactionRepository.save(transaction);

            // creating json data to send in string format
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fromUser",transaction.getFromUser());
            jsonObject.put("toUser",transaction.getToUser());
            jsonObject.put("amount",transaction.getAmount());
            jsonObject.put("transactionId", transaction.getTransactionId());

            // converting json to string
            String kafkamsg = objectMapper.writeValueAsString(jsonObject);

            // sending to wallet service to update wallets
            kafkaTemplate.send("update-wallet",kafkamsg);

        }
        catch (Exception e){
            throw e;
        }

    }

    @KafkaListener(topics = "update_transaction",groupId = "test1234")
    public void updateTransaction(String message) throws JsonProcessingException {

        try {
            //Decode the message and update values and save to db
            JSONObject transactionRequest = objectMapper.readValue(message,JSONObject.class);

            String transactionStatus = (String) transactionRequest.get("status");
            String transactionId = (String) transactionRequest.get("transactionId");

            Transaction t = transactionRepository.findByTransactionId(transactionId);

            t.setTransactionStatus(TransactionStatus.valueOf(transactionStatus));

            transactionRepository.save(t);

            // CALL NOTIFICATION SERVICE AND SEND EMAILS
            callNotificationService(t);

        }
        catch (Exception e){
            throw e;
        }

    }

    public void callNotificationService(Transaction transaction){

        // taking required parameters
        String fromUserName  = transaction.getFromUser();
        String toUserName = transaction.getToUser();
        String transactionId = transaction.getTransactionId();

        // to get information of users for furthur processing
        HttpEntity httpEntity = new HttpEntity(new HttpHeaders());

        // fetching sender info
        URI url = URI.create("http://localhost:9999/user/get_user/"+fromUserName);
        JSONObject fromUserObject = restTemplate.exchange(url, HttpMethod.GET,httpEntity,JSONObject.class).getBody();

        String senderName = (String)fromUserObject.get("name");
        String senderEmail = (String)fromUserObject.get("email");

        //fetching receiver info
        url = URI.create("http://localhost:9999/user/get_user/"+toUserName);
        JSONObject toUserObject = restTemplate.exchange(url, HttpMethod.GET,httpEntity,JSONObject.class).getBody();

        String receiverEmail = (String)toUserObject.get("email");
        String receiverName = (String)toUserObject.get("name");


        //SEND THE EMAIL AND MESSAGE TO NOTIFICATIONS-SERVICE VIA KAFKA
        JSONObject emailRequest = new JSONObject();

        // to sender
        emailRequest.put("email",senderEmail);

        String SenderMessageBody = String.format("Hi %s \n" +
                        "The transcation with transactionId %s has been %s of Rs %d . ",
                senderName,transactionId,transaction.getTransactionStatus(),transaction.getAmount());
        emailRequest.put("message",SenderMessageBody);

        String message = emailRequest.toString();

        kafkaTemplate.send("send_email",message);

        // if transaction is not success then send only to
        if(transaction.getTransactionStatus().equals("FAILED")){
            return;
        }

        //SEND an email to the reciever also
        JSONObject emailRequest2 = new JSONObject();
        emailRequest2.put("email",receiverEmail);

        String receiverMessageBody = String.format("Hi %s you have recived money %d from %s",
                receiverName,transaction.getAmount(),senderName);
        emailRequest2.put("message",receiverMessageBody);

        message = emailRequest2.toString();

        kafkaTemplate.send("send_email",message);

    }
}
