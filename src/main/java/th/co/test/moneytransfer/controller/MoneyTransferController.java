package th.co.test.moneytransfer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import th.co.test.moneytransfer.request.AccountRequest;
import th.co.test.moneytransfer.request.AccountStatusRequest;
import th.co.test.moneytransfer.response.AccountResponse;
import th.co.test.moneytransfer.response.BalanceResponse;
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
}
