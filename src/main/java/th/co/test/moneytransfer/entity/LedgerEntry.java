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
@Table(name = "ledger_entry")
public class LedgerEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long accountId;
	private Long transferId;
	private String entryType;
	private BigDecimal amount;
	private BigDecimal balanceAfter;
	private LocalDateTime createdAt;

	@PrePersist //set default value
	protected void onCreate() {
	    createdAt = LocalDateTime.now();
	}

}
