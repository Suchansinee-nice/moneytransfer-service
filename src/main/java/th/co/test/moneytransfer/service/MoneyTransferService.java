package th.co.test.moneytransfer.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import th.co.test.moneytransfer.entity.Account;
import th.co.test.moneytransfer.entity.LedgerEntry;
import th.co.test.moneytransfer.exception.AccountCloseNotAllowedException;
import th.co.test.moneytransfer.exception.AccountNotFoundException;
import th.co.test.moneytransfer.repository.AccountRepository;
import th.co.test.moneytransfer.repository.LedgerEntryRepository;
import th.co.test.moneytransfer.request.AccountRequest;
import th.co.test.moneytransfer.request.AccountStatusRequest;
import th.co.test.moneytransfer.response.AccountResponse;
import th.co.test.moneytransfer.response.BalanceResponse;

@Service
@RequiredArgsConstructor
public class MoneyTransferService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Account account = new Account();
        
        //mock account number
        account.setAccountNumber("TMP" + UUID.randomUUID().toString().replace("-", "").substring(0, 17));
        account.setOwnerName(request.getOwnerName());
        account.setCurrency(request.getCurrency());
        account.setBalance(request.getInitialBalance());

        // saveAndFlush -> insert immediately
        Account saved = accountRepository.saveAndFlush(account);

        //update account number after insert data
        String accountNumber = String.format("%010d", saved.getId());
        saved.setAccountNumber(accountNumber);
        saved = accountRepository.save(saved);

        //save to ledger
        if (saved.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            LedgerEntry ledger = new LedgerEntry();
            ledger.setAccountId(saved.getId());
            ledger.setAmount(saved.getBalance());
            ledger.setBalanceAfter(saved.getBalance());
            ledger.setEntryType("CREDIT");
            ledger.setTransferId(null);
            
            ledgerEntryRepository.save(ledger);
        }
  

        AccountResponse response = new AccountResponse();
        response.setId(saved.getId());
        response.setAccountNumber(saved.getAccountNumber());
        response.setOwnerName(saved.getOwnerName());
        response.setCurrency(saved.getCurrency());
        response.setBalance(saved.getBalance());
        response.setStatus(saved.getStatus());
        response.setCreatedAt(saved.getCreatedAt());

        return response;
    }

    public AccountResponse getAccountById(Long id) {
    	Optional<Account> result = accountRepository.findById(id);

        if (result.isEmpty()) {
            throw new AccountNotFoundException(id);
        }

        Account account = result.get();

        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setOwnerName(account.getOwnerName());
        response.setCurrency(account.getCurrency());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setCreatedAt(account.getCreatedAt());

        return response;
    }

    @Transactional
    public AccountResponse updateAccountStatus(Long id, AccountStatusRequest request) {
        Optional<Account> result = accountRepository.findById(id);

        if (result.isEmpty()) {
            throw new AccountNotFoundException(id);
        }

        Account account = result.get();

        // Cannot close account if balance != 0
        if ("CLOSED".equals(request.getStatus()) && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountCloseNotAllowedException(id);
        }

        account.setStatus(request.getStatus());
        Account saved = accountRepository.save(account);

        AccountResponse response = new AccountResponse();
        response.setId(saved.getId());
        response.setAccountNumber(saved.getAccountNumber());
        response.setOwnerName(saved.getOwnerName());
        response.setCurrency(saved.getCurrency());
        response.setBalance(saved.getBalance());
        response.setStatus(saved.getStatus());
        response.setCreatedAt(saved.getCreatedAt());

        return response;
    }

    public BalanceResponse getAccountBalance(Long id) {
        Optional<Account> result = accountRepository.findById(id);

        if (result.isEmpty()) {
            throw new AccountNotFoundException(id);
        }

        Account account = result.get();

        BalanceResponse response = new BalanceResponse();
        response.setAccountId(account.getId());
        response.setBalance(account.getBalance());
        response.setCurrency(account.getCurrency());
        response.setAsOf(LocalDateTime.now());

        return response;
    }
}
