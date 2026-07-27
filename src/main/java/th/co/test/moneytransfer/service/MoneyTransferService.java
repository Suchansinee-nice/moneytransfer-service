package th.co.test.moneytransfer.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import th.co.test.moneytransfer.entity.Account;
import th.co.test.moneytransfer.entity.LedgerEntry;
import th.co.test.moneytransfer.entity.Transfer;
import th.co.test.moneytransfer.exception.AccountCloseNotAllowedException;
import th.co.test.moneytransfer.exception.AccountNotActiveException;
import th.co.test.moneytransfer.exception.AccountNotFoundException;
import th.co.test.moneytransfer.exception.InsufficientBalanceException;
import th.co.test.moneytransfer.exception.InvalidPageRequestException;
import th.co.test.moneytransfer.exception.RateLimitExceededException;
import th.co.test.moneytransfer.exception.TransferNotFoundException;
import th.co.test.moneytransfer.model.AccountCacheModel;
import th.co.test.moneytransfer.model.LedgerEntryModel;
import th.co.test.moneytransfer.repository.AccountRepository;
import th.co.test.moneytransfer.repository.LedgerEntryRepository;
import th.co.test.moneytransfer.repository.TransferRepository;
import th.co.test.moneytransfer.request.AccountRequest;
import th.co.test.moneytransfer.request.AccountStatusRequest;
import th.co.test.moneytransfer.request.DepositRequest;
import th.co.test.moneytransfer.request.TransferRequest;
import th.co.test.moneytransfer.request.WithDrawRequest;
import th.co.test.moneytransfer.response.AccountResponse;
import th.co.test.moneytransfer.response.BalanceResponse;
import th.co.test.moneytransfer.response.DepositResponse;
import th.co.test.moneytransfer.response.TransactionResponse;
import th.co.test.moneytransfer.response.TransferResponse;
import th.co.test.moneytransfer.response.WithdrawResponse;

@Log4j2
@Service
@RequiredArgsConstructor
public class MoneyTransferService {

    private static final String ACCOUNT_CACHE_KEY_PREFIX = "account:";
    private static final Duration ACCOUNT_CACHE_TTL = Duration.ofSeconds(60);

    private static final String TRANSFER_RATE_LIMIT_KEY_PREFIX = "ratelimit:transfer:";
    private static final int TRANSFER_RATE_LIMIT_MAX_REQUESTS = 10;
    private static final Duration TRANSFER_RATE_LIMIT_WINDOW = Duration.ofSeconds(60);

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransferRepository transferRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

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
        // 1. read from cache first
        String cacheKey = accountCacheKey(id);
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);

        if (cachedJson != null) {
            try {
                AccountCacheModel cached = objectMapper.readValue(cachedJson, AccountCacheModel.class);
                
                //get Balance real time not from cache
                Optional<BigDecimal> balanceResult = accountRepository.findBalanceById(id);

                if (balanceResult.isEmpty()) {
                    throw new AccountNotFoundException(id);
                }

                BigDecimal balance = balanceResult.get();

                AccountResponse response = new AccountResponse();
                response.setId(id);
                response.setAccountNumber(cached.getAccountNumber());
                response.setOwnerName(cached.getOwnerName());
                response.setCurrency(cached.getCurrency());
                response.setStatus(cached.getStatus());
                response.setCreatedAt(cached.getCreatedAt());
                response.setBalance(balance);

                return response;
            } catch (JsonProcessingException e) {
                log.warn("ไม่สามารถ parse account cache key={} ได้ ข้ามไปอ่านจาก DB แทน", cacheKey, e);
            }
        }

        // 2.cache miss -> read from DB
        Optional<Account> result = accountRepository.findById(id);

        if (result.isEmpty()) {
            throw new AccountNotFoundException(id);
        }

        Account account = result.get();

        //3. read from db -> keep in cache
        cacheAccount(account);
        
        //4.set response
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

    private void cacheAccount(Account account) {
        AccountCacheModel cacheModel = new AccountCacheModel();
        cacheModel.setAccountNumber(account.getAccountNumber());
        cacheModel.setOwnerName(account.getOwnerName());
        cacheModel.setCurrency(account.getCurrency());
        cacheModel.setStatus(account.getStatus());
        cacheModel.setCreatedAt(account.getCreatedAt());

        try {
        	//convert object to string (json)
            String json = objectMapper.writeValueAsString(cacheModel);
            //keep in redis
            redisTemplate.opsForValue().set(accountCacheKey(account.getId()), json, ACCOUNT_CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.warn("ไม่สามารถเก็บ account id={} ลง cache ได้", account.getId(), e);
        }
    }

    private void evictAccountCache(Long id) {
        redisTemplate.delete(accountCacheKey(id));
    }

    private String accountCacheKey(Long id) {
        return ACCOUNT_CACHE_KEY_PREFIX + id;
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

        // update status -> remove cache 
        evictAccountCache(id);

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

    @Transactional
    public DepositResponse deposit(Long id, DepositRequest request) {
        Optional<Account> result = accountRepository.findById(id);

        if (result.isEmpty()) {
            throw new AccountNotFoundException(id);
        }

        Account account = result.get();

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountNotActiveException(id);
        }

        // balance + amount and update to account
        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        Account savedAccount = accountRepository.save(account);

        evictAccountCache(id);

        // insert ledger_entry
        LedgerEntry ledger = new LedgerEntry();
        ledger.setAccountId(savedAccount.getId());
        ledger.setAmount(request.getAmount());
        ledger.setBalanceAfter(savedAccount.getBalance());
        ledger.setEntryType("CREDIT");
        ledger.setTransferId(null);
        LedgerEntry savedLedger = ledgerEntryRepository.save(ledger);
        
        //set response
        DepositResponse response = new DepositResponse();
        response.setAccountId(savedAccount.getId());
        response.setBalance(savedAccount.getBalance());
        response.setLedgerEntryId(savedLedger.getId());

        return response;
    }

    @Transactional
    public WithdrawResponse withdraw(Long id, WithDrawRequest request) {
        Optional<Account> result = accountRepository.findById(id);

        if (result.isEmpty()) {
            throw new AccountNotFoundException(id);
        }

        Account account = result.get();

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountNotActiveException(id);
        }

        // check if balance < amount throw error
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(id);
        }

        // balance - amount and update to account by id
        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        Account savedAccount = accountRepository.save(account);


        evictAccountCache(id);

        // insert ledger_entry
        LedgerEntry ledger = new LedgerEntry();
        ledger.setAccountId(savedAccount.getId());
        ledger.setAmount(request.getAmount());
        ledger.setBalanceAfter(savedAccount.getBalance());
        ledger.setEntryType("DEBIT");
        ledger.setTransferId(null);
        LedgerEntry savedLedger = ledgerEntryRepository.save(ledger);

        WithdrawResponse response = new WithdrawResponse();
        response.setAccountId(savedAccount.getId());
        response.setBalance(savedAccount.getBalance());
        response.setLedgerEntryId(savedLedger.getId());

        return response;
    }
    
    
    public TransactionResponse getTransactions(Long id, int page, int size) {
        Optional<Account> result = accountRepository.findById(id);

        if (result.isEmpty()) {
            throw new AccountNotFoundException(id);
        }

        // check page and size
        if (page < 0) {
            throw new InvalidPageRequestException("page ต้องเริ่มต้นที่ 0 ขึ้นไป");
        }
        if (size < 1 || size > 100) {
            throw new InvalidPageRequestException("size ต้องมีค่าอยู่ระหว่าง 1 ถึง 100");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerEntry> ledgerPage = ledgerEntryRepository.findByAccountId(id, pageable);

        List<LedgerEntryModel> items = new ArrayList<LedgerEntryModel>();
        for (LedgerEntry entry : ledgerPage.getContent()) {
            LedgerEntryModel item = new LedgerEntryModel();
            item.setId(entry.getId());
            item.setEntryType(entry.getEntryType());
            item.setAmount(entry.getAmount());
            item.setBalanceAfter(entry.getBalanceAfter());
            item.setTransferId(entry.getTransferId());
            item.setCreatedAt(entry.getCreatedAt());
            items.add(item);
        }

        TransactionResponse response = new TransactionResponse();
        response.setAccountId(id);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(ledgerPage.getTotalElements());
        response.setTotalPages(ledgerPage.getTotalPages());
        response.setItems(items);

        return response;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // 1. check rate limit
        checkTransferRateLimit(request.getFromAccountId());

        // if not found account
        Optional<Account> fromResult = accountRepository.findById(request.getFromAccountId());
        if (fromResult.isEmpty()) {
            throw new AccountNotFoundException(request.getFromAccountId());
        }

        Optional<Account> toResult = accountRepository.findById(request.getToAccountId());
        if (toResult.isEmpty()) {
            throw new AccountNotFoundException(request.getToAccountId());
        }
        
        //found account
        Account fromAccount = fromResult.get();
        Account toAccount = toResult.get();

        // check validate 
        String failureReason = null;

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            failureReason = "ห้ามโอนเข้าบัญชีตัวเอง";
        } else if (!"ACTIVE".equals(fromAccount.getStatus())) {
            failureReason = "บัญชีต้นทางไม่ได้อยู่ในสถานะ ACTIVE";
        } else if (!"ACTIVE".equals(toAccount.getStatus())) {
            failureReason = "บัญชีปลายทางไม่ได้อยู่ในสถานะ ACTIVE";
        } else if (!fromAccount.getCurrency().equals(request.getCurrency())
                || !toAccount.getCurrency().equals(request.getCurrency())) {
            failureReason = "สกุลเงินไม่ตรงกับบัญชี";
        } else if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            failureReason = "ยอดเงินในบัญชีต้นทางไม่เพียงพอ";
        }

        if (failureReason != null) {
   
            Transfer failed = new Transfer();
         
            failed.setIdempotencyKey(UUID.randomUUID().toString());
            failed.setRequestHash(UUID.randomUUID().toString());
            failed.setFromAccountId(request.getFromAccountId());
            failed.setToAccountId(request.getToAccountId());
            failed.setAmount(request.getAmount());
            failed.setCurrency(request.getCurrency());
            failed.setStatus("FAILED");
            failed.setFailureReason(failureReason);
            Transfer savedFailed = transferRepository.save(failed);

            return buildTransferResponse(savedFailed);
        }

        // Pass all condition -> save
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

  
        evictAccountCache(fromAccount.getId());
        evictAccountCache(toAccount.getId());


        //save to transfer
        Transfer transfer = new Transfer();
        
        transfer.setIdempotencyKey(UUID.randomUUID().toString());
        transfer.setRequestHash(UUID.randomUUID().toString());
        transfer.setFromAccountId(request.getFromAccountId());
        transfer.setToAccountId(request.getToAccountId());
        transfer.setAmount(request.getAmount());
        transfer.setCurrency(request.getCurrency());
        transfer.setStatus("COMPLETED");
        Transfer savedTransfer = transferRepository.saveAndFlush(transfer);
        
        
        //save to ledger entry
        LedgerEntry debit = new LedgerEntry();
        debit.setAccountId(fromAccount.getId());
        debit.setTransferId(savedTransfer.getId());
        debit.setEntryType("DEBIT");
        debit.setAmount(request.getAmount());
        debit.setBalanceAfter(fromAccount.getBalance());
        ledgerEntryRepository.save(debit);

        LedgerEntry credit = new LedgerEntry();
        credit.setAccountId(toAccount.getId());
        credit.setTransferId(savedTransfer.getId());
        credit.setEntryType("CREDIT");
        credit.setAmount(request.getAmount());
        credit.setBalanceAfter(toAccount.getBalance());
        ledgerEntryRepository.save(credit);

        return buildTransferResponse(savedTransfer);
    }

    private void checkTransferRateLimit(Long accountId) {
        String key = TRANSFER_RATE_LIMIT_KEY_PREFIX + accountId;

        // 1st time if don't have key in redis -> create key and add count 1
        Long count = redisTemplate.opsForValue().increment(key);

        //count == 1
        if (count != null && count == 1L) {
            redisTemplate.expire(key, TRANSFER_RATE_LIMIT_WINDOW);
        }
        
        //count > 10
        if (count != null && count > TRANSFER_RATE_LIMIT_MAX_REQUESTS) {
        	//get remaining time
            Long ttlSeconds = redisTemplate.getExpire(key);
            
            long retryAfterSeconds;
            if (ttlSeconds != null && ttlSeconds > 0) {
                retryAfterSeconds = ttlSeconds;
            } else {
                retryAfterSeconds = TRANSFER_RATE_LIMIT_WINDOW.getSeconds();
            }

            throw new RateLimitExceededException(accountId, retryAfterSeconds);
        }
    }

    public TransferResponse getTransferById(Long id) {
        Optional<Transfer> result = transferRepository.findById(id);

        if (result.isEmpty()) {
            throw new TransferNotFoundException(id);
        }

        return buildTransferResponse(result.get());
    }

    private TransferResponse buildTransferResponse(Transfer transfer) {
        
    	//set to response
    	TransferResponse response = new TransferResponse();
        response.setTransferId(transfer.getId());
        response.setStatus(transfer.getStatus());
        response.setFromAccountId(transfer.getFromAccountId());
        response.setToAccountId(transfer.getToAccountId());
        response.setAmount(transfer.getAmount());
        response.setCurrency(transfer.getCurrency());
        response.setCreatedAt(transfer.getCreatedAt());
        return response;
    }
}
