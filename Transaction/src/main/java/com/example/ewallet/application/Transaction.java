package com.example.ewallet.application;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String fromUser;

    private String toUser;

    private int amount;

    private String transactionId = UUID.randomUUID().toString();

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private String purpose;

    private Date transactionDate;
}
