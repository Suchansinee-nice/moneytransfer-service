package th.co.test.moneytransfer.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DepositRequest {
	
	@NotNull(message = "amount ห้ามว่าง")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount ต้องมากกว่า 0")
	private BigDecimal amount;

}
