package com.bank.account.service;

import com.bank.account.controller.AccountV1;
import com.bank.account.controller.BalanceV1;
import com.bank.account.model.Account;
import com.bank.account.model.Balance;
import com.bank.account.model.Currency;
import com.bank.account.model.ExchangeRates;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.BalanceRepository;
import com.bank.account.repository.CurrencyRepository;
import com.bank.account.repository.ExchangeRatesRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountControllerService {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;

    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRatesRepository exchangeRatesRepository;

    public AccountControllerService(AccountRepository accountRepository, BalanceRepository balanceRepository, CurrencyRepository currencyRepository, ExchangeRatesRepository exchangeRatesRepository) {
        this.accountRepository = accountRepository;
        this.balanceRepository = balanceRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeRatesRepository = exchangeRatesRepository;
    }

    public AccountV1 getAccount(String iban) {
        Optional<Account> account = accountRepository.findByIban(iban);
        List<Balance>  b = balanceRepository.findByAccountIban(iban);

        if (!account.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with IBAN " + iban + " not found");
        }
        AccountV1 accountV1 = toAccountV1(account.get());
        return accountV1;
    }

    private AccountV1 toAccountV1(Account account) {
        AccountV1 accountV1 = new AccountV1();
        accountV1.iban = account.getIban();
        accountV1.name = account.getName();
        if (!account.getBalances().isEmpty()) {
            List<BalanceV1> balanceV1s = new ArrayList<>();
            List<Balance> balances = account.getBalances();
            balances.forEach( b -> {
                BalanceV1 balanceV1 = new BalanceV1();
                balanceV1.amount = b.getAmount();
                balanceV1.currency =  BalanceV1.CurrencyCode.valueOf( b.getCurrency().getCurrencyCode());
                balanceV1s.add(balanceV1);
            });
            accountV1.balanceV1s = balanceV1s;
        }
        return accountV1;
    }

    public String addToBalance(String iban, BalanceV1 balanceV1) {
        if (balanceV1.amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Added amount can not be empty");
        }
        if (balanceV1.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Added amount can not be 0 or negative");
        }
        if (balanceV1.currency == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Added currency can not be empty");
        }
        Optional<Currency> currency = currencyRepository.findByCurrencyCode(balanceV1.currency.name());
        if (!currency.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Currency with code " + balanceV1.currency.name() + " not found");
        }

        Optional<Account> account = accountRepository.findByIban(iban);
        if (!account.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with IBAN " + iban + " not found");
        }
        Optional<Balance> balance = account.get().getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals(balanceV1.currency.name())).findFirst();

        if (balance.isPresent()) {
            balance.get().setAmount(balance.get().getAmount().add(balanceV1.amount));
            balanceRepository.save(balance.get());
            return "Successfully added balance";
        } else {
            Balance newBalance = new Balance();
            newBalance.setAmount(balanceV1.amount);
            newBalance.setCurrency(currency.get());
            newBalance.setAccount(account.get());
            balanceRepository.save(newBalance);
            accountRepository.save(account.get());
            return "Successfully created new balance and added to it";
        }
    }

    public String debitFromBalance(String iban, BalanceV1 balanceV1) {
        if (balanceV1.amount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debited amount can not be empty");
        }
        if (balanceV1.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debited amount can not be 0 or negative");
        }

        if (balanceV1.currency == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debited currency can not be empty");
        }
        Optional<Currency> currency = currencyRepository.findByCurrencyCode(balanceV1.currency.name());
        if (!currency.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Currency with code " + balanceV1.currency.name() + " not found");
        }
        Optional<Account> account = accountRepository.findByIban(iban);
        if (!account.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with IBAN " + iban + " not found");
        }

        Optional<Balance> balance = account.get().getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals(balanceV1.currency.name())).findFirst();

        if (balance.isPresent()) {
            if (balance.get().getAmount().subtract(balanceV1.amount).compareTo(BigDecimal.ZERO) >= 0) {
                balance.get().setAmount(balance.get().getAmount().subtract(balanceV1.amount));
                balanceRepository.save(balance.get());
                String logResult = postLogging(balanceV1);
                if (logResult != null && !logResult.contains("This domain is for use in documentation examples without needing permission")) { // When return contains: "This domain is ..", treating it as correct response from GET
                    return "Successfully debited from balance. Log result: " +  logResult;
                }
                return "Successfully debited from balance";
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account does not have enough "+ balanceV1.currency.name() + " on balance");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account does not have " + balanceV1.currency.name() + " balance");
        }
    }

    public String currencyExchange(String iban, String from,  String to, String stringAmount) {
        if (stringAmount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exchanged amount can not be empty");
        }
        BigDecimal amount = BigDecimal.valueOf(0);
        try {
            amount = toMoney(stringAmount);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exchanged amount must be number value");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Exchanged amount can not be negative or 0");
        }

        Optional<Account> account = accountRepository.findByIban(iban);
        if (!account.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account with IBAN " + iban + " not found");
        }
        Optional<Currency> currencyFrom = currencyRepository.findByCurrencyCode(from);
        if (!currencyFrom.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Currency with code" + from + " not found");
        }
        Optional<Currency> currencyTo = currencyRepository.findByCurrencyCode(to);
        if (!currencyTo.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CCurrency with code " + to + " not found");
        }
        if (currencyTo.get().getCurrencyCode().equals(currencyFrom.get().getCurrencyCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency 'from' and currency 'to' can not be same");
        }

        Optional<Balance> balance = account.get().getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals(from)).findFirst();

        if (balance.isPresent()) {
            if ((balance.get().getAmount().subtract(amount)).compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal toAmount = convertFromTo(currencyFrom, currencyTo, amount);
                if (toAmount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No exchange rate between " + currencyFrom.get().getCurrencyCode() + " and " +  currencyTo.get().getCurrencyCode());
                }
                Optional<Balance> balanceTo = account.get().getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals(to)).findFirst();

                if (balanceTo.isPresent()) {
                    balanceTo.get().setAmount(balanceTo.get().getAmount().add(toAmount));
                    balanceRepository.save(balanceTo.get());
                } else {
                    Balance newBalance = new Balance();
                    newBalance.setAmount(toAmount);
                    newBalance.setCurrency(currencyTo.get());
                    newBalance.setAccount(account.get());
                    balanceRepository.save(newBalance);
                }
                balance.get().setAmount(balance.get().getAmount().subtract(amount));
                balanceRepository.save(balance.get());
                return "Successfully exchanged " + amount + " " + from + " to " + toAmount + " " + to ;
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account does not have enough "+ from + " on balance");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account does not have " + from + " currency balance");
        }
    }

    private String postLogging(BalanceV1 balanceV1) {
        try {
            String responseEntity = execute("http://www.example.com/", HttpMethod.GET, balanceV1, String.class);// Using GET for this test site because POST gives METHOD_NOT-ALLOWED error

            String result;
            if (responseEntity != null) {
                result = responseEntity.toString();
                return result;
            }
            return null;
        } catch (Exception e) {
            return "Error logging debit from balance: " + e;
        }
    }

    private <T> T execute(String url, HttpMethod httpMethod, Object event, Class<T> responseClass) {
        org.springframework.http.HttpHeaders headers;
        headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<T> response;
        try {
            RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.exchange(url, httpMethod, new HttpEntity<>(event, headers), responseClass);

        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Logging call failed with status: " + e.getStatusCode() + ", statusText: " + e.getStatusText() + ", error: " + e.getResponseBodyAsString(), e);
        }
        return response.getBody();
    }

    private BigDecimal convertFromTo(Optional<Currency> currencyFrom,  Optional<Currency> currencyTo, BigDecimal amount) {
        BigDecimal toAmount = BigDecimal.valueOf(0);
        Optional<ExchangeRates> rate = exchangeRatesRepository.findRate(currencyFrom.get(), currencyTo.get());
        if (rate.isPresent()) {
            toAmount = amount.multiply(rate.get().getRate()).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
        } else {
            rate = exchangeRatesRepository.findRate(currencyTo.get(), currencyFrom.get());
            if (rate.isPresent()) {
                toAmount = amount.divide(rate.get().getRate()).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
            } else {
                // No rate pair found, throw error
                toAmount = BigDecimal.valueOf(-1);
            }
        }
        return toAmount;
    }

    public static BigDecimal toMoney(String amount) {
        return new BigDecimal(amount).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
    }

}
