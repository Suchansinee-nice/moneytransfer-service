package th.co.test.moneytransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import th.co.test.moneytransfer.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = "SELECT NEXT VALUE FOR account_number_seq", nativeQuery = true)
    Long getNextAccountNumberSeqValue();
}
