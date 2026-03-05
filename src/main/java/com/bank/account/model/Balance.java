package com.bank.account.model;

import jakarta.persistence.*;

@Entity
@Table(name = "balance")
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = Account.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "account", referencedColumnName = "id", insertable = true, updatable = false)
    private Account account;

    private Double amount;

    @ManyToOne(targetEntity = Currency.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "currency", referencedColumnName = "id", insertable = true, updatable = true)
    private Currency currency;

    public Balance() {}

    public Account getAccount() {
        return account;
    }


    public void setAccount(Account account) {
        this.account = account;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
