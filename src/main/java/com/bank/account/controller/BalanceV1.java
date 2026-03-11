package com.bank.account.controller;

import java.math.BigDecimal;

public class BalanceV1 {

    public BigDecimal amount;

    public CurrencyCode currency;

    public enum CurrencyCode {
        EUR, USD, GBP, SEK
    }
}
