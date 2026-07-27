package th.co.test.moneytransfer.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import th.co.test.moneytransfer.entity.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(value = "SELECT NEXT VALUE FOR account_number_seq", nativeQuery = true)
    Long getNextAccountNumberSeqValue();

    @Query("select a.balance from Account a where a.id = :id")
    Optional<BigDecimal> findBalanceById(Long id);
}
