package com.vutran.expensetracker.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;

// Annotation này biến class thành "Trạm kiểm soát" bắt mọi lỗi văng ra từ Controller
@RestControllerAdvice 
public class GlobalExceptionHandler {

    // 1. Bắt các lỗi do logic nghiệp vụ của chúng ta tự ném ra (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()) // 400 Bad Request
                .message(ex.getMessage()) // Lấy đúng câu chữ mình đã throw ("Không tìm thấy Category!")
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 2. Bắt TẤT CẢ các lỗi hệ thống không lường trước được (Exception chung)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500 Internal Server Error
                .message("System error. Please try again later!")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
    // Lấy câu thông báo lỗi đầu tiên mà nó tìm thấy
    String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .message(errorMessage)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
}