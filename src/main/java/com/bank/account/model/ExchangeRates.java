package com.bank.account.model;

import jakarta.persistence.*;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = Currency.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "fromc", referencedColumnName = "id", insertable = true, updatable = true)
    private Currency fromc;

    @ManyToOne(targetEntity = Currency.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "toc", referencedColumnName = "id", insertable = true, updatable = true)
    private Currency toc;

    private Double rate;

    public ExchangeRates() {}

    public Currency getFromc() {
        return fromc;
    }

    public void setFromc(Currency fromc) {
        this.fromc = fromc;
    }

    public Currency getToc() {
        return toc;
    }

    public void setToc(Currency toc) {
        this.toc = toc;
    }

    public Double getRate() {
        return rate;
    }

}
