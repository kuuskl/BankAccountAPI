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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.bank.account.BankAccount.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AccountControllerIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private AccountController accountController;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAccountBalance() {
        AccountV1 accountV1 = accountController.get("EE120000012345678901");
        assertEquals("EE120000012345678901", accountV1.iban,  "Wrong IBAN");
        assertEquals(3,  accountV1.balanceV1s.size(), "Wrong number of balances on account");
        assertEquals(BalanceV1.CurrencyCode.EUR, accountV1.balanceV1s.get(0).currency, "Wrong currency code on EUR balance");
        assertEquals(new BigDecimal("100.00"), accountV1.balanceV1s.get(0).amount, "Wrong amount on EUR balance");
    }

    @Test
    void getAccountBalanceWrongIban() {
        try {
            AccountV1 accountV1 = accountController.get("EE120000012345678900");
            assertNull(accountV1);
        } catch (ResponseStatusException e) {
            assertEquals("404 NOT_FOUND \"Account with IBAN EE120000012345678900 not found\"", e.getMessage());
        }
    }

    @Test
    void addToBalance() {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong starting balance on account, must be 100");
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.EUR;
        balanceV1.amount = BigDecimal.valueOf(100);

        String result = accountController.addToBalance("EE120000012345678901", balanceV1);
        assertEquals("Successfully added balance", result,"Wrong result code from addToBalance");
        account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(new BigDecimal("200.00"), account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), "Wrong end balance on account, must be 200");
    }

    @Test
    void addToBalanceNewCurrencyBalance() {
        Account account = accountRepository.findByIban("EE120000012345678902").get();
        assert account.getBalances().isEmpty() : "Account should not have any balances";
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.USD;
        balanceV1.amount = BigDecimal.valueOf(100);

        String result = accountController.addToBalance("EE120000012345678902", balanceV1);
        assertEquals("Successfully created new balance and added to it", result, "Wrong result code from addToBalance");

        List<Balance> balance = balanceRepository.findByAccountIban("EE120000012345678902");
        assertEquals(1, balance.size(), "Wrong number of balances on account, should be 1");
        assertEquals(BigDecimal.valueOf(100), balance.stream().filter(b -> b.getCurrency().getCurrencyCode().equals("USD")).findFirst().get().getAmount(), "Wrong end balance on account, must be 100");
        assertEquals("USD", balance.get(0).getCurrency().getCurrencyCode(), "Wrong currency on new balance, must be USD");
    }

    @Test
    void addToBalanceEmptyAmount() {
        Account account = accountRepository.findByIban("EE120000012345678902").get();
        assert account.getBalances().isEmpty() : "Account should not have any balances";
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.USD;
        try {
            String result = accountController.addToBalance("EE120000012345678902", balanceV1);
        } catch (ResponseStatusException e) {
            assertEquals("400 BAD_REQUEST \"Added amount can not be empty\"", e.getMessage(),"Wrong result code from addToBalance");
        }
        List<Balance> balance = balanceRepository.findByAccountIban("EE120000012345678902");
        assert !balance.isEmpty() : "New Balance should not exist";
    }

    @Test
    void debitFromBalance() {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong starting balance on account, must be 100");
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.EUR;
        balanceV1.amount = BigDecimal.valueOf(50);

        String result = accountController.debitFromBalance("EE120000012345678901", balanceV1);
        assertEquals("Successfully debited from balance", result.substring(0, 33), "Wrong result code from debitFromBalance");

        account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(new BigDecimal("50.00"), account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(),  "Wrong end balance on account, must be 50");
    }

    @Test
    void debitFromBalanceNotEnoughCurrency() {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong starting balance on account, must be 100");
        BalanceV1 balanceV1 = new BalanceV1();
        balanceV1.currency = BalanceV1.CurrencyCode.EUR;
        balanceV1.amount = BigDecimal.valueOf(200);

        try {
            String result = accountController.debitFromBalance("EE120000012345678901", balanceV1);
        } catch (ResponseStatusException e) {
            assertEquals("400 BAD_REQUEST \"Account does not have enough EUR on balance\"", e.getMessage(),"Wrong result code from debitFromBalance");
        }

        account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong starting balance on account, must be 100");
    }


    @Test
    void exchangeCurrency()  {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong EUR starting balance on account, must be 100");
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("GBP")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong GBP starting balance on account, must be 100");

        String result = accountController.exchangeCurrency("EE120000012345678901", "EUR", "GBP", "10");
        assertEquals("Successfully exchanged 10.00 EUR to 8.70 GBP", result, "Wrong result code from exchangeCurrency");

        List<Balance> balance = balanceRepository.findByAccountIban("EE120000012345678901");
        assertEquals(balance.stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("90.00"),  "Wrong EUR end balance on account, must be 90");
        assertEquals(balance.stream().filter(b -> b.getCurrency().getCurrencyCode().equals("GBP")).findFirst().get().getAmount(), new BigDecimal("108.70"),  "Wrong GBP end balance on account, must be 108.7");
    }

    @Test
    void exchangeCurrencyNewBalance()  {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong EUR starting balance on account, must be 100");
        assertTrue( account.getBalances().stream().noneMatch(b -> b.getCurrency().getCurrencyCode().equals("USD")), "Wrong USD starting balance on account");

        String result = accountController.exchangeCurrency("EE120000012345678901", "EUR", "USD", "10");
        assertEquals("Successfully exchanged 10.00 EUR to 11.70 USD", result, "Wrong result code from exchangeCurrency");

        List<Balance> balance = balanceRepository.findByAccountIban("EE120000012345678901");
        assertEquals(balance.stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("90.00"),  "Wrong EUR end balance on account, must be 90");
        assertEquals(balance.stream().filter(b -> b.getCurrency().getCurrencyCode().equals("USD")).findFirst().get().getAmount(), new BigDecimal("11.70"),  "Wrong USD end balance on account, must be 90");
    }

    @Test
    void exchangeCurrencyNotEnoughBaseCurrency()  {
        Account account = accountRepository.findByIban("EE120000012345678901").get();
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong EUR starting balance on account, must be 100");
        assertEquals(account.getBalances().stream().filter(b -> b.getCurrency().getCurrencyCode().equals("GBP")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "Wrong GBP starting balance on account, must be 100");

        try {
            String result = accountController.exchangeCurrency("EE120000012345678901", "EUR", "GBP", "200");
        } catch (ResponseStatusException e) {
            assertEquals("400 BAD_REQUEST \"Account does not have enough EUR on balance\"", e.getMessage(),"Wrong result code from exchangeCurrency");
        }

        List<Balance> balance = balanceRepository.findByAccountIban("EE120000012345678901");
        assertEquals(balance.stream().filter(b -> b.getCurrency().getCurrencyCode().equals("EUR")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "EUR Balance should not have changed");
        assertEquals(balance.stream().filter(b -> b.getCurrency().getCurrencyCode().equals("GBP")).findFirst().get().getAmount(), new BigDecimal("100.00"),  "GBP Balance should not have changed");
    }

    @Test
    void getAccount() throws Exception {
        mockMvc.perform(get("/api/account/EE120000012345678901"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("AccountOne"));
    }

    @Test
    void putAddToAccount() throws Exception {
        String json = "{\"amount\":\"1000\",\"currency\":\"EUR\"}";

        mockMvc.perform(put("/api/account/add/EE120000012345678901")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully added balance"));
    }

    @Test
    void putDebitFromAccount() throws Exception {
        String json = "{\"amount\":\"100\",\"currency\":\"EUR\"}";

        mockMvc.perform(put("/api/account/debit/EE120000012345678901")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully debited from balance"));
    }

    @Test
    void putExchangeCurrency() throws Exception {
        mockMvc.perform(put("/api/account/exchange/EE120000012345678901/EUR/USD/10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully exchanged 10.00 EUR to 11.70 USD"));
    }

}
