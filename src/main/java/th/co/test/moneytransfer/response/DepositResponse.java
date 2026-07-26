package th.co.test.moneytransfer.response;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponse {
	
	private Long accountId;
	private BigDecimal balance;
	private Long ledgerEntryId;

}
