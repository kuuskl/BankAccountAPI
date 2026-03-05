package com.bank.account.repository;

import com.bank.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query( "SELECT a FROM Account a WHERE a.iban = :iban")
    Optional<Account> findByIban(@Param("iban") String iban);
}
