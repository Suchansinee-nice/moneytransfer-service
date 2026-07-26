package th.co.test.moneytransfer.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TransferRequest {
	
	@NotNull(message = "fromAccountId ห้ามว่าง")
	private Long fromAccountId;
	
	@NotNull(message = "toAccountId ห้ามว่าง")
	private Long toAccountId;
	
	@NotNull(message = "amount ห้ามว่าง")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount ต้องมากกว่า 0")
	private BigDecimal amount;
	
	@NotBlank(message = "currency ห้ามว่าง")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency ไม่ถูกต้อง")
	private String currency;
	

}
