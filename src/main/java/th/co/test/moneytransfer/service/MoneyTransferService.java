package th.co.test.moneytransfer.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import th.co.test.moneytransfer.entity.Account;
import th.co.test.moneytransfer.entity.LedgerEntry;
import th.co.test.moneytransfer.repository.AccountRepository;
import th.co.test.moneytransfer.repository.LedgerEntryRepository;
import th.co.test.moneytransfer.request.AccountRequest;
import th.co.test.moneytransfer.response.AccountResponse;

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
  

        return AccountResponse.builder()
                .id(saved.getId())
                .accountNumber(saved.getAccountNumber())
                .ownerName(saved.getOwnerName())
                .currency(saved.getCurrency())
                .balance(saved.getBalance())
                .status(saved.getStatus())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
