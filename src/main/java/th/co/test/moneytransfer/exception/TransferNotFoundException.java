package th.co.test.moneytransfer.exception;

public class TransferNotFoundException extends RuntimeException {

    public TransferNotFoundException(Long transferId) {
        super("ไม่พบรายการโอนเงิน id: " + transferId);
    }
}
