package th.co.test.moneytransfer.exception;

public class AccountCloseNotAllowedException extends RuntimeException {

    public AccountCloseNotAllowedException(Long accountId) {
        super("ไม่สามารถปิดบัญชี id: " + accountId + " ได้ เนื่องจากยังมียอดคงเหลืออยู่");
    }
}
