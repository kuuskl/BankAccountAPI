package com.bank.account.model;

import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String iban;
    private String name;

    @OneToMany(targetEntity = Balance.class, fetch = FetchType.EAGER, cascade = {CascadeType.DETACH, CascadeType.REFRESH} , mappedBy = "account")
    private List<Balance> balances;

    public Account() {}

    public Account(String iban, String name) {
        this.iban = iban;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Balance> getBalances() { return balances; }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    public void addBalance(Balance balance) {
        if (this.balances == null) {
            this.balances = new ArrayList<>();
        }
        this.balances.add(balance);
    }
}
