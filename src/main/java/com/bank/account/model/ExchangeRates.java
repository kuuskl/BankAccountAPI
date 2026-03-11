package com.bank.account.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = Currency.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "fromc", referencedColumnName = "id", insertable = true, updatable = true)
    private Currency fromc;

    @ManyToOne(targetEntity = Currency.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "toc", referencedColumnName = "id", insertable = true, updatable = true)
    private Currency toc;

    @Column(name = "rate", precision = 19, scale = 2, nullable = false)
    private BigDecimal rate;

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

    public BigDecimal getRate() {
        return rate;
    }

}
