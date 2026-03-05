package com.bank.account.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "currency")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String currencyCode;

    @OneToMany(targetEntity = ExchangeRates.class, fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH} , mappedBy = "fromc")
    private List<ExchangeRates> from;

    @OneToMany(targetEntity = ExchangeRates.class, fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.REFRESH} , mappedBy = "toc")
    private List<ExchangeRates> to;

    public Currency() {}

    public Currency(String currencyCode, Long conversionRate) {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {}

    public String getCurrencyCode() {
        return currencyCode;
    }
    public void setCurrencyCode(String currencyCode) {}

    public List<ExchangeRates> getFrom() { return from; }

    public void setFrom(List<ExchangeRates> from) {
        this.from = from;
    }

    public void addFrom(ExchangeRates from) {
        if (this.from == null) {
            this.from = new ArrayList<>();
        }
        this.from.add(from);
    }

    public List<ExchangeRates> getTo() { return to; }

    public void setTo(List<ExchangeRates> to) {
        this.to = to;
    }

    public void addTo(ExchangeRates to) {
        if (this.to == null) {
            this.to = new ArrayList<>();
        }
        this.to.add(to);
    }

}
