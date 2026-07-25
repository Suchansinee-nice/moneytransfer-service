package th.co.test.moneytransfer.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String aggregateType;
	private String aggregateId;
	private String eventType;

	@Lob
	private String payload;

	private String status;
	private LocalDateTime createdAt;
	private LocalDateTime publishedAt;

	@PrePersist //set default value
	protected void onCreate() {
	    if (status == null) status = "PENDING";
	    createdAt = LocalDateTime.now();
	}

}
