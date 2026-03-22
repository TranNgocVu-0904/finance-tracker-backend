package com.vutran.expensetracker.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;

//This annotation turns the class into a checkpoint that catches all errors thrown from the Controller.
@RestControllerAdvice 
public class GlobalExceptionHandler {

    // 1. Catch errors thrown by business logic (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value()) // 400 Bad Request
                .message(ex.getMessage()) // Lấy đúng câu chữ mình đã throw ("Không tìm thấy Category!")
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 2. Catch all unforeseen system errors (General Exceptions)
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
    // Get the first error message that it couldn't find
    String errorMessage = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .message(errorMessage)
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
}
}