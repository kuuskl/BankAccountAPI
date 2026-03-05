import com.bank.account.controller.AccountController;
import com.bank.account.controller.AccountV1;
import com.bank.account.controller.BalanceV1;
import com.bank.account.model.Account;
import com.bank.account.model.Balance;
import com.bank.account.repository.AccountRepository;

import com.bank.account.repository.BalanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest(classes = com.bank.account.BankAccount.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public class AccountControllerIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private AccountController accountController;

    @Test
    void getAccountBalance() {
        AccountV1 accountV1 = accountController.get("EE120000012345678901");
        assert accountV1.iban.equals("EE120000012345678901") : "Wrong IBAN";
        assert accountV1.balanceV1s.size() == 3 : "Wrong number of balances on account";
        assert accountV1.balanceV1s.get(0).currency == BalanceV1.CurrencyCode.EUR : "Wrong currency code on EUR balance";
        assert accountV1.balanceV1s.get(0).amount == 100 : "Wrong amount on EUR balance";
    }

    @Test
    void getAccountBalanceWrongIban() {
        AccountV1 accountV1 = accountController.get("EE120000012345678900");
        assert accountV1.iban == null : "Account with IBAN EE120000012345678900 should not exist";
    }

    @Test
    void addToBalance() {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 100 : "Wrong starting balance on account, must be 100";
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.EUR;
        balanceV1.amount = 100d;

        String result = accountController.addToBalance("EE120000012345678901", balanceV1);
        assert result.equals("Successfully added balance") : "Wrong result code from addToBalance";

        account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 200 : "Wrong end balance on account, must be 200";
    }

    @Test
    void addToBalanceNewCurrencyBalance() {
        Account account = accountRepository.findByIban("EE120000012345678902").get();
        assert account.getBalances().isEmpty() : "Account should not have any balances";
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.USD;
        balanceV1.amount = 100d;

        String result = accountController.addToBalance("EE120000012345678902", balanceV1);
        assert result.equals("Successfully created new balance and added to it") : "Wrong result code from addToBalance";

        List<Balance> balance = balanceRepository.findByIban("EE120000012345678902");
        assert balance.get(0).getAmount() == 100 : "Wrong end balance on account, must be 100";
        assert balance.get(0).getCurrency().getCurrencyCode().equals("USD") : "Wrong currency on new balance, must be USD";
    }

    @Test
    void addToBalanceEmptyAmount() {
        Account account = accountRepository.findByIban("EE120000012345678902").get();
        assert account.getBalances().isEmpty() : "Account should not have any balances";
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.USD;

        String result = accountController.addToBalance("EE120000012345678902", balanceV1);
        assert result.equals("Added amount can not be empty") : "Wrong result code from addToBalance";

        List<Balance> balance = balanceRepository.findByIban("EE120000012345678902");
        assert balance.get(0) == null  : "New Balance should not exist";// For some reason its a list of 1 element and the element is null
    }

    @Test
    void debitFromBalance() {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 100 : "Wrong starting balance on account, must be 100";
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.EUR;
        balanceV1.amount = 50d;

        String result = accountController.debitFromBalance("EE120000012345678901", balanceV1);
        assert result.equals("Successfully debited from balance") : "Wrong result code from debitFromBalance";

        account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 50 : "Wrong end balance on account, must be 50";
    }

    @Test
    void debitFromBalanceNotEnoughCurrency() {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 100 : "Wrong starting balance on account, must be 100";
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.EUR;
        balanceV1.amount = 200d;

        String result = accountController.debitFromBalance("EE120000012345678901", balanceV1);
        assert result.equals("Account does not have enough EUR on balance") : "Wrong result code from debitFromBalance";

        account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 100 : "Wrong end balance on account, must be 100";
    }


    @Test
    void exchangeCurrency()  {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 100 : "Wrong EUR starting balance on account, must be 100";
        assert account.getBalances().get(1).getAmount() == 100 : "Wrong GBP starting balance on account, must be 100";

        String result = accountController.exchangeCurrency("EE120000012345678901", "EUR", "GBP", 10d);
        assert result.equals("Successfully exchanged 10.0 EUR to 8.7 GBP") : "Wrong result code from exchangeCurrency";

        List<Balance> balance = balanceRepository.findByIban("EE120000012345678901");
        assert balance.get(0).getAmount() == 90 : "Wrong end balance on account, must be 90";
        assert balance.get(1).getAmount() == 108.7 : "Wrong end balance on account, must be 108.7";
    }

    @Test
    void exchangeCurrencyNewBalance()  {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 100 : "Wrong EUR starting balance on account, must be 100";

        assert account.getBalances().stream().noneMatch(b -> b.getCurrency().getCurrencyCode().equals("USD")) : "Wrong USD starting balance on account, must be 100";

        String result = accountController.exchangeCurrency("EE120000012345678901", "EUR", "USD", 10d);
        assert result.equals("Successfully exchanged 10.0 EUR to 11.7 USD") : "Wrong result code from exchangeCurrency";

        List<Balance> balance = balanceRepository.findByIban("EE120000012345678901");
        assert balance.get(0).getAmount() == 90 : "Wrong end balance on account, must be 90";
        assert balance.get(3).getAmount() == 11.7 : "Wrong end balance on account, must be 11.7";
    }

    @Test
    void exchangeCurrencyNotEnoughBaseCurrency()  {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assert account.getBalances().get(0).getAmount() == 100 : "Wrong EUR starting balance on account, must be 100";
        assert account.getBalances().get(1).getAmount() == 100 : "Wrong GBP starting balance on account, must be 100";

        String result = accountController.exchangeCurrency("EE120000012345678901", "EUR", "GBP", 200d);
        assert result.equals("Account does not have enough EUR on balance") : "Wrong result code from exchangeCurrency";

        List<Balance> balance = balanceRepository.findByIban("EE120000012345678901");
        assert balance.get(0).getAmount() == 100 : "EUR Balance should not have changed";
        assert balance.get(1).getAmount() == 100 : "GBP Balance should not have changed";
    }
}
