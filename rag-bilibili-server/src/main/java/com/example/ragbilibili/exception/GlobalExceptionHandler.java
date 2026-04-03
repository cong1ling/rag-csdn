package com.example.ragbilibili.exception;

import com.example.ragbilibili.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 参数校验异常（@Valid 触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数错误");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.error(ErrorCode.PARAM_ERROR.getCode(), message));
    }

    /**
     * 唯一键冲突异常
     * 通过异常消息区分不同表的唯一键约束
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException e) {
        String msg = e.getMessage();
        int code;
        String message;
        int httpStatus;

        if (msg != null && msg.contains("uk_user_bvid")) {
            code = ErrorCode.VIDEO_ALREADY_EXISTS.getCode();
            message = ErrorCode.VIDEO_ALREADY_EXISTS.getMessage();
            httpStatus = ErrorCode.VIDEO_ALREADY_EXISTS.getHttpStatus();
        } else {
            code = ErrorCode.USERNAME_EXISTS.getCode();
            message = ErrorCode.USERNAME_EXISTS.getMessage();
            httpStatus = ErrorCode.USERNAME_EXISTS.getHttpStatus();
        }
        return ResponseEntity
                .status(httpStatus)
                .body(Result.error(code, message));
    }

    /**
     * 业务异常 — 携带正确的 HTTP 状态码
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(Result.error(e.getErrorCode().getCode(), e.getMessage()));
    }

    /**
     * 未知异常兜底 — 不向前端暴露内部错误细节
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage()));
    }
}
