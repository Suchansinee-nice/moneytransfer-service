package th.co.test.moneytransfer.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId) {
        super("ไม่พบบัญชี id: " + accountId);
    }
}
