package cn.net.explorer.exception;

import cn.dev33.satoken.exception.SaTokenException;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.util.ThrowableUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 全局异常处理类
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ApiResponse<?> dealMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("捕获业务异常处理：{}", e.getMessage());
        log.error(ThrowableUtil.getStackTrace(e));

        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        String message = allErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(";"));
        return ApiResponse.fail(message);
    }
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> bussinessException(BusinessException businessException) {
        log.error("捕获业务异常处理：{}", businessException.getMessage());
        log.error(ThrowableUtil.getStackTrace(businessException));
        businessException.printStackTrace();
        String message = businessException.getMessage();
        return ApiResponse.fail(message);
    }

    @ExceptionHandler(SaTokenException.class)
    public ApiResponse<?> satokenExcetption(SaTokenException exception) {
        log.error("捕获全局异常处理：{}", exception.getMessage());
        log.error(ThrowableUtil.getStackTrace(exception));
        exception.printStackTrace();
        String message = exception.getMessage();
        return ApiResponse.fail(HttpStatus.UNAUTHORIZED.value(), message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> exception(Exception exception) {
        log.error("捕获全局异常处理：{}", exception.getMessage());
        log.error(ThrowableUtil.getStackTrace(exception));
        exception.printStackTrace();
        String message = exception.getMessage();
        return ApiResponse.fail(message);
    }



}
