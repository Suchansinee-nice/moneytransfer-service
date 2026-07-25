package th.co.test.moneytransfer.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "account")
public class Account {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String accountNumber;
	private String ownerName;
	private String currency;
	private BigDecimal balance;
	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	@PrePersist //set default value
	protected void onCreate() {
	    if (balance == null) balance = BigDecimal.ZERO;
	    if (status == null) status = "ACTIVE";
	    createdAt = LocalDateTime.now();
	    updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate //update date everytime when update 
	protected void onUpdate() {
	    updatedAt = LocalDateTime.now();
	}
	

}
