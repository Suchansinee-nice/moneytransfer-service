package th.co.test.moneytransfer.exception;

public class RateLimitExceededException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(Long accountId, long retryAfterSeconds) {
        super("บัญชี id: " + accountId + " เรียกใช้งานเกิน rate limit ที่กำหนด กรุณาลองใหม่อีกครั้งภายหลัง");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
