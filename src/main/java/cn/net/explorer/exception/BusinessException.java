package cn.net.explorer.exception;


import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private Integer errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
