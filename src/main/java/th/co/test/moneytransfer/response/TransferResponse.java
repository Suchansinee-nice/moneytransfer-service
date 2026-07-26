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
public class TransferResponse {
	
	private Long transferId;
	private String status;
	private Long fromAccountId;
	private Long toAccountId;
	private BigDecimal amount;
	private String currency;
	private LocalDateTime createdAt;

}
