package com.example.ewallet.application;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class TransactionRequestDto {

    private String fromUser;

    private String toUser;

    private int amount;

    private String purpose;
}
