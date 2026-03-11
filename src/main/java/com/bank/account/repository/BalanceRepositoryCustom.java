package com.bank.account.repository;

import com.bank.account.model.Balance;

import java.util.List;

public interface BalanceRepositoryCustom {
    List<Balance> findByAccountIban(String iban);
}
