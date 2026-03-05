INSERT INTO currency (currency_code) VALUES ('EUR');
INSERT INTO currency (currency_code) VALUES ('USD');
INSERT INTO currency (currency_code) VALUES ('GBP');
INSERT INTO currency (currency_code) VALUES ('SEK');

INSERT INTO account (iban, name) VALUES ('EE120000012345678901', 'AccountOne');
INSERT INTO account (iban, name) VALUES ('EE120000012345678902', 'AccountTwo');

INSERT INTO balance (account, amount, currency) VALUES (1, 100, 1);
INSERT INTO balance (account, amount, currency) VALUES (1, 100, 3);
INSERT INTO balance (account, amount, currency) VALUES (1, 100, 4);

INSERT INTO exchange_rates (fromc, toc, rate) VALUES (1, 2, 1.17);
INSERT INTO exchange_rates (fromc, toc, rate) VALUES (1, 3, 0.87);
INSERT INTO exchange_rates (fromc, toc, rate) VALUES (1, 4, 10.70);
INSERT INTO exchange_rates (fromc, toc, rate) VALUES (2, 3, 0.75);
INSERT INTO exchange_rates (fromc, toc, rate) VALUES (2, 4, 9.14);
INSERT INTO exchange_rates (fromc, toc, rate) VALUES (3, 4, 12.24);

