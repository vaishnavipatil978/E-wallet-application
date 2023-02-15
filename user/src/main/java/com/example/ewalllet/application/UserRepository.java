package com.example.ewalllet.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User,Integer> {

  @Query(value = "Select * FROM users WHERE username=:user",nativeQuery = true)
   public  User findByUsername(String user);

}
