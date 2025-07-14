package org.example.config;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.R;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验失败", e);
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return R.error("参数校验失败: " + String.join(", ", errors));
    }

    /**
     * 处理Bean校验异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleBindException(BindException e) {
        log.error("参数绑定失败", e);
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        return R.error("参数绑定失败: " + String.join(", ", errors));
    }

    /**
     * 处理约束校验异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("约束校验失败", e);
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        return R.error("约束校验失败: " + String.join(", ", errors));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("非法参数", e);
        return R.error("非法参数: " + e.getMessage());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return R.error("系统内部错误");
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return R.error("系统运行异常: " + e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleException(Exception e) {
        log.error("系统异常", e);
        return R.error("系统异常: " + e.getMessage());
    }
} 