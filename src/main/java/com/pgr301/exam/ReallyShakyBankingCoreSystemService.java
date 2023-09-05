package com.pgr301.exam;

import com.pgr301.exam.model.Account;
import com.pgr301.exam.model.Transaction;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static java.math.BigDecimal.valueOf;
import static java.util.Optional.ofNullable;


/**
 * This class simulates a class that would normall invokce methods on the Core banking system either through htto
 * or a proproetary protocol.
 * <p>
 * NO MODIFICATION OF THIS CLASS IS ALLOWED!
 */
@Component
class ReallyShakyBankingCoreSystemService implements BankingCoreSystmeService {

    private Map<String, Account> theBank = new HashMap();

    @Override
    public void transfer(Transaction tx, String fromAccount, String toAccount) {
        Account from = getOrCreateAccount(fromAccount);
        Account to = getOrCreateAccount(toAccount);
        from.setBalance(from.getBalance().subtract(valueOf(tx.getAmount())));
        to.setBalance(to.getBalance().add(valueOf(tx.getAmount())));
    }

    @Override
    public Account updateAccount(Account a) {
        Account account = getOrCreateAccount(a.getId());
        account.setBalance(a.getBalance());
        account.setCurrency(a.getCurrency());
        theBank.put(a.getId(), a);
        return account;
    }

    @Override
    public BigDecimal balance(@PathVariable String accountId) {
        Account account = ofNullable(theBank.get(accountId)).orElseThrow(BankAccountController.AccountNotFoundException::new);
        return account.getBalance();
    }

    @Override
    public Account getAccount(String accountNumber) {
        return getOrCreateAccount(accountNumber);
    }

    private Account getOrCreateAccount(String accountId) {
        if (theBank.get(accountId) == null) {
            Account a = new Account();
            a.setId(accountId);
            theBank.put(accountId, a);
        }
        return theBank.get(accountId);
    }

}
