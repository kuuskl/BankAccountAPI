# BankAccountAPI

REST api for bank account handling in Java, Spring Framework. 

Api functionality : get account balance, add money to account, debit money from account, currency exchange.



## Api functionality details:

### Get account balance.
Returns account information and accounts balances.

Api call:

curl http://localhost:8080/api/account/{iban}

Examples:

curl http://localhost:8080/api/account/EE120000012345678901

curl http://localhost:8080/api/account/EE120000012345678902


### Add to accounts balance.
Adds money specified in payload currency to account balance. If account does not have a balance with the currency, the balance is created.

Parameters:

String iban : Account IBAN to add money to

json payload : "{\"amount\":\"Number\",\"currency\":\"String\"}"

Api call:

curl -X PUT -H "Content-Type: application/json" -d "{\"amount\":\"Number\",\"currency\":\"String\"}" http://localhost:8080/api/account/add/{iban}

Examples:

curl -X PUT -H "Content-Type: application/json" -d "{\"amount\":\"1000\",\"currency\":\"EUR\"}" http://localhost:8080/api/account/add/EE120000012345678901

curl -X PUT -H "Content-Type: application/json" -d "{\"amount\":\"100\",\"currency\":\"USD\"}" http://localhost:8080/api/account/add/EE120000012345678902


### Debit from accounts balance.
Debits money specified in payload currency from balance. Balance must have enough money in selected currecy.

Parameters:

String iban : Account IBAN to debit money from

json payload : "{\"amount\":\"Number\",\"currency\":\"String\"}"

Api call:

curl -X PUT -H "Content-Type: application/json" -d "{\"amount\":\"Number\",\"currency\":\"String\"}" http://localhost:8080/api/debit/add/{iban}

Examples:

curl -X PUT -H "Content-Type: application/json" -d "{\"amount\":\"100\",\"currency\":\"USD\"}" http://localhost:8080/api/account/debit/EE120000012345678901

curl -X PUT -H "Content-Type: application/json" -d "{\"amount\":\"50\",\"currency\":\"EUR\"}" http://localhost:8080/api/account/debit/EE120000012345678901


### Exchange currency on an account.
Exchanages from one currency to another on an account. Account must have enough currecny to convert from. 

Parameters:

String iban : Account IBAN to convert money on

String from : Currency code of the money to convert from

String to : Currency code of the money to convert to

String amount : The amount in 'from' currency to convert

Api call:

curl -X PUT -H "Content-Type: application/json" http://localhost:8080/api/account/exchange/{iban}/{from}/{to}/{amount}

Example:

curl -X PUT -H "Content-Type: application/json" http://localhost:8080/api/account/exchange/EE120000012345678901/EUR/USD/10.1


## Running the application:
From IntelliJ: Run BankAccount (or use Maven run)

From command line (in BankAccountAPI folder): mvn spring-boot:run

To shut down from command line:

Press Ctrl+C in that same terminal to stop it.
