package th.co.test.moneytransfer.model;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AccountCacheModel {

    private String accountNumber;
    private String ownerName;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}
