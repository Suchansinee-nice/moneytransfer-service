package th.co.test.moneytransfer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AccountStatusRequest {
	
	@NotBlank(message = "status ห้ามว่าง")
    @Pattern(regexp = "FROZEN|CLOSED", message = "status ไม่ถูกต้อง")
	private String status;

}
