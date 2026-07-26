package th.co.test.moneytransfer.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import th.co.test.moneytransfer.model.LedgerEntryModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
	
	private Long accountId;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private List<LedgerEntryModel> items;

}
