package com.example.ewallet.application;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {


    @Autowired
    private JavaMailSender javaMailSender;
    // for sending mail

    @Autowired
    ObjectMapper objectMapper;
    // to convert json

    @Value("${spring.mail.username}")
    private String sender;


    @KafkaListener(topics = "send_email", groupId = "test1234")
    public void sendEmailMessage(String message) throws JsonProcessingException, MessagingException {

        // convert string message to json
        JSONObject emailRequest = objectMapper.readValue(message,JSONObject.class);

        //Get the email and message from JSONObject
        String receiverEmail = (String)emailRequest.get("email");
        String messageBody = (String)emailRequest.get("message");

        // creating new mail
        SimpleMailMessage mailMessage
                = new SimpleMailMessage();

        mailMessage.setFrom(sender);
        mailMessage.setTo(receiverEmail);
        mailMessage.setText(messageBody);
        mailMessage.setSubject("(Testing Application) New Transaction");

        // sending the mail
        javaMailSender.send(mailMessage);
    }
}

