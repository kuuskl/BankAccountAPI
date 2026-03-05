package com.bank.account.repository;

import com.bank.account.model.Currency;
import com.bank.account.model.ExchangeRates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExchangeRatesRepository extends JpaRepository<ExchangeRates, Long> {
    @Query( "SELECT a FROM ExchangeRates a WHERE a.fromc = :fromc and a.toc = :toc")
    Optional<ExchangeRates> findRate(@Param("fromc") Currency fromc, @Param("toc") Currency toc);

}
