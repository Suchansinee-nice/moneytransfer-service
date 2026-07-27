package th.co.test.moneytransfer.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import th.co.test.moneytransfer.service.MoneyTransferService;

@Log4j2
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MoneyTransferController {

    private final MoneyTransferService moneyTransferService;

    @PostMapping(value = "/accounts")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = moneyTransferService.createAccount(request);
        log.info("create account: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/accounts/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id) {
        AccountResponse response = moneyTransferService.getAccountById(id);
        log.info("get account: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/accounts/{id}/balance")
    public ResponseEntity<BalanceResponse> getAccountBalance(@PathVariable Long id) {
        BalanceResponse response = moneyTransferService.getAccountBalance(id);
        log.info("get account balance: {}", response);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/accounts/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(@PathVariable Long id,
                                                                 @Valid @RequestBody AccountStatusRequest request) {
        AccountResponse response = moneyTransferService.updateAccountStatus(id, request);
        log.info("update account status: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/accounts/{id}/deposit")
    public ResponseEntity<DepositResponse> deposit(@PathVariable Long id,
                                                     @Valid @RequestBody DepositRequest request) {
        DepositResponse response = moneyTransferService.deposit(id, request);
        log.info("deposit: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/accounts/{id}/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(@PathVariable Long id,
                                                       @Valid @RequestBody WithDrawRequest request) {
        WithdrawResponse response = moneyTransferService.withdraw(id, request);
        log.info("withdraw: {}", response);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<TransactionResponse> getTransactions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
    	TransactionResponse response = moneyTransferService.getTransactions(id, page, size);
    	log.info("transaction : {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/transfers")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = moneyTransferService.transfer(request);
        log.info("transfer: {}", response);

        if ("FAILED".equals(response.getStatus())) {
            return ResponseEntity.unprocessableEntity().body(response);
        }

        URI location = URI.create("/api/v1/transfers/" + response.getTransferId());
        return ResponseEntity.created(location).body(response);
    }
}
