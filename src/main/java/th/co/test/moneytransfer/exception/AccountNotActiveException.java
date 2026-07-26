package th.co.test.moneytransfer.exception;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(Long accountId) {
        super("บัญชี id: " + accountId + " ไม่ได้อยู่ในสถานะ ACTIVE");
    }
}
