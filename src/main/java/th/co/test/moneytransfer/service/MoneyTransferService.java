package th.co.test.moneytransfer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import th.co.test.moneytransfer.entity.Account;
import th.co.test.moneytransfer.repository.AccountRepository;
import th.co.test.moneytransfer.request.AccountRequest;
import th.co.test.moneytransfer.response.AccountResponse;

@Service
@RequiredArgsConstructor
public class MoneyTransferService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        Long nextSeq = accountRepository.getNextAccountNumberSeqValue();
        String accountNumber = String.format("%010d", nextSeq);

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setOwnerName(request.getOwnerName());
        account.setCurrency(request.getCurrency());
        account.setBalance(request.getInitialBalance());

        Account saved = accountRepository.save(account);

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
