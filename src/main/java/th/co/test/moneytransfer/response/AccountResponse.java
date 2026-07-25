package th.co.test.moneytransfer.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String ownerName;
    private String currency;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
}
