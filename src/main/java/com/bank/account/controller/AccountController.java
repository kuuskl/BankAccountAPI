package com.bank.account.controller;

import com.bank.account.service.AccountControllerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountControllerService accountControllerService;

    public AccountController(AccountControllerService accountControllerService) {
        this.accountControllerService = accountControllerService;
    }

    @GetMapping("/{iban}")
    public AccountV1 get(@PathVariable String iban) {
        return accountControllerService.getAccount(iban);
    }

    @PutMapping("add/{iban}")
    public String addToBalance(@PathVariable String iban, @RequestBody BalanceV1 balance) {
        return accountControllerService.addToBalance(iban, balance);
    }

    @PutMapping("debit/{iban}")
    public String debitFromBalance(@PathVariable String iban, @RequestBody BalanceV1 balance) {
        return accountControllerService.debitFromBalance(iban, balance);
    }

    @PutMapping("exchange/{iban}/{from}/{to}/{amount}")
    public String exchangeCurrency(@PathVariable String iban, @PathVariable String from, @PathVariable String to, @PathVariable String amount) {
        return accountControllerService.currencyExchange(iban, from, to, amount);
    }

}