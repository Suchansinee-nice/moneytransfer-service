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
public class BalanceResponse {
	
	private Long accountId;
	private BigDecimal balance;
	private String currency;
	private LocalDateTime asOf;
	

}
