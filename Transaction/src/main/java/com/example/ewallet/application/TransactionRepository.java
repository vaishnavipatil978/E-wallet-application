package com.example.ewallet.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransactionRepository extends JpaRepository<Transaction,Integer> {

    @Query(value = "SELECT * FROM transactions WHERE transaction_id=:tId",nativeQuery = true)
    public Transaction findByTransactionId(String tId);
}
