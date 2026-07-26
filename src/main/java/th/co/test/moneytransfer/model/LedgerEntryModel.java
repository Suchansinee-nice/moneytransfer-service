package th.co.test.moneytransfer.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LedgerEntryModel {
	
	private Long id;
    private String entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Long transferId;
    private LocalDateTime createdAt;

}
