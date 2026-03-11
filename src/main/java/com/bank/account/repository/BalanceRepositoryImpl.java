package com.bank.account.repository;


import com.bank.account.model.Balance;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BalanceRepositoryImpl implements BalanceRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Balance> findByAccountIban(String iban) {
        TypedQuery<Balance> q = em.createQuery(
                "SELECT b FROM Balance b RIGHT JOIN Account a ON b.account.id = a.id WHERE a.iban = :iban", Balance.class);
        q.setParameter("iban", iban);

        return q.getResultList();
    }
}