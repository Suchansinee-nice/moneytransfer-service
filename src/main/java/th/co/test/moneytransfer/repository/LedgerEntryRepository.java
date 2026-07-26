package th.co.test.moneytransfer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import th.co.test.moneytransfer.entity.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long>{

    Page<LedgerEntry> findByAccountId(Long accountId, Pageable pageable);
}
