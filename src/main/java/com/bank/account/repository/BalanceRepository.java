package com.bank.account.repository;

import com.bank.account.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    @Query( "SELECT b FROM Balance b RIGHT JOIN Account a ON b.account.id = a.id WHERE a.iban = :iban")
    List<Balance> findByIban(@Param("iban") String iban);
}
