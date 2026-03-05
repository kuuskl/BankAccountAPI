package com.bank.account.controller;

public class BalanceV1 {

    public Double amount;

    public CurrencyCode currency;

    public enum CurrencyCode {
        EUR, USD, GBP, SEK
    }
}
