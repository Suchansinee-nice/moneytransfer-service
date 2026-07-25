package th.co.test.moneytransfer.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AccountRequest {

    @NotBlank(message = "ownerName ห้ามว่าง")
    private String ownerName;

    @NotBlank(message = "currency ห้ามว่าง")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency ไม่ถูกต้อง")
    private String currency;
    
    @DecimalMin(value = "0.0", inclusive = true, message = "initialBalance ต้องไม่ติดลบ")
    private BigDecimal initialBalance;
}
