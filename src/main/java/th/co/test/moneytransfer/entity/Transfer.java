package th.co.test.moneytransfer.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "transfer")
public class Transfer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String idempotencyKey;
	private Long fromAccountId;
	private Long toAccountId;
	private BigDecimal amount;
	private String currency;
	private String status;
	private String requestHash;
	private String failureReason;
	private LocalDateTime createdAt;

	@PrePersist //set default value
	protected void onCreate() {
	    if (status == null) status = "PENDING";
	    createdAt = LocalDateTime.now();
	}

}
