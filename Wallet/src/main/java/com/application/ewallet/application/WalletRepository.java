package com.application.ewallet.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WalletRepository extends JpaRepository<Wallet,Integer> {

    @Query(value = "SELECT * FROM wallet WHERE username=:user",nativeQuery = true)
    public Wallet findWalletByUsername(String user);

}
