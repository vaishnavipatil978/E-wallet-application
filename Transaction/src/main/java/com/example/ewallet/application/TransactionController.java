package com.example.ewallet.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @PostMapping("/transaction")
    public ResponseEntity createTransaction(@RequestBody TransactionRequestDto transactionRequestDto){

        try {
           transactionService.createTransaction(transactionRequestDto);
            return new ResponseEntity("Transaction in process...Please check inbox for furthur update",HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity("Transaction cannot be made!",HttpStatus.BAD_REQUEST);
        }
    }
}
