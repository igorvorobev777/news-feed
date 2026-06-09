package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionCatcher {

    private final HttpServletRequest request;

    public ExceptionCatcher(HttpServletRequest request) {
        this.request = request;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(err -> err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        return errorResponse(HttpStatus.BAD_REQUEST, "Validation Error", message);
    }
    
    @ExceptionHandler(AccessException.class)
    public ResponseEntity<ErrorResponse> handleAccessError(AccessException ex) {
        return errorResponse(HttpStatus.FORBIDDEN, "Ошибка доступа", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return errorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, "Incorrect request", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        System.err.println("Unexpected error: " + ex.getMessage());
        ex.printStackTrace();
        
        return errorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Internal Server Error", 
            "An unexpected error occurred"
        );
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String error, String message) {
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            error,
            message,
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(Exception ex) {
        return errorResponse(
            HttpStatus.CONFLICT, 
             "Конфликт версий",
             "Ресурс был изменён другим пользователем. Пожалуйста, перезагрузите и попробуйте снова."
        );
    }
}