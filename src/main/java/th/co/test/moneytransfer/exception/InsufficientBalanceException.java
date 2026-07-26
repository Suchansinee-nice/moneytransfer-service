package th.co.test.moneytransfer.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(Long accountId) {
        super("บัญชี id: " + accountId + " มียอดคงเหลือไม่เพียงพอสำหรับการถอน");
    }
}
