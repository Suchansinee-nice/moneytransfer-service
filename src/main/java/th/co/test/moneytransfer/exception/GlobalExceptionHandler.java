package th.co.test.moneytransfer.exception;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import th.co.test.moneytransfer.filter.RequestIdFilter;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_BASE_URI = "https://errors.bank.local/";

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                                HttpServletRequest request) {
        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                ERROR_BASE_URI + "invalid-body",
                "Invalid request body",
                "request body ผิดรูปแบบ หรือ parse ไม่ได้",
                request);
        return respond(problem);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleAccountNotFound(AccountNotFoundException ex,
                                                                  HttpServletRequest request) {
        ProblemDetail problem = buildProblem(
                HttpStatus.NOT_FOUND,
                ERROR_BASE_URI + "account-not-found",
                "Account not found",
                ex.getMessage(),
                request);
        return respond(problem);
    }

    @ExceptionHandler(AccountCloseNotAllowedException.class)
    public ResponseEntity<ProblemDetail> handleAccountCloseNotAllowed(AccountCloseNotAllowedException ex,
                                                                        HttpServletRequest request) {
        ProblemDetail problem = buildProblem(
                HttpStatus.CONFLICT,
                ERROR_BASE_URI + "account-close-not-allowed",
                "Account close not allowed",
                ex.getMessage(),
                request);
        return respond(problem);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ProblemDetail> handleAccountNotActive(AccountNotActiveException ex,
                                                                   HttpServletRequest request) {
        ProblemDetail problem = buildProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ERROR_BASE_URI + "account-not-active",
                "Account not active",
                ex.getMessage(),
                request);
        return respond(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("ข้อมูลที่ส่งมาไม่ถูกต้อง");

        ProblemDetail problem = buildProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ERROR_BASE_URI + "validation-failed",
                "Validation failed",
                detail,
                request);
        return respond(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error", ex);

        ProblemDetail problem = buildProblem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ERROR_BASE_URI + "internal-error",
                "Internal server error",
                "เกิดข้อผิดพลาดที่ไม่คาดคิด กรุณาลองใหม่อีกครั้ง",
                request);
        return respond(problem);
    }

    private ProblemDetail buildProblem(HttpStatus status, String type, String title, String detail,
                                        HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("traceId", getTraceId(request));
        return problem;
    }

    private ResponseEntity<ProblemDetail> respond(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(problem);
    }

    private String getTraceId(HttpServletRequest request) {
        Object requestId = request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE);
        return requestId != null ? requestId.toString() : "unknown";
    }
}
