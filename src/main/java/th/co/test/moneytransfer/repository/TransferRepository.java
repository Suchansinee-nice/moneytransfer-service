package th.co.test.moneytransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import th.co.test.moneytransfer.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long>{

}
