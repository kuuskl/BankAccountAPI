package com.bank.account.repository;

import com.bank.account.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    @Query( "SELECT a FROM Currency a WHERE a.currencyCode = :currencyCode")
    Optional<Currency> findByCurrencyCode(@Param("currencyCode") String currencyCode);
}
