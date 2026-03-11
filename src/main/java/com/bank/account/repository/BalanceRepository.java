package com.bank.account.repository;

import com.bank.account.model.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<Balance, Long>, BalanceRepositoryCustom {
}
