package com.alotrabong.shared.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex){
    String msg = ex.getBindingResult().getAllErrors().stream()
      .findFirst().map(e -> e.getDefaultMessage()).orElse("Validation error");
    return ResponseEntity.badRequest().body(api(false, msg, null));
  }

  @ExceptionHandler(AppException.class)
  public ResponseEntity<?> handleApp(AppException ex){
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(api(false, ex.getMessage(), null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex){
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(api(false, "Internal error", null));
  }

  private Map<String,Object> api(boolean success, String message, Object data){
    return Map.of(
      "success", success,
      "message", message != null ? message : "Unknown error",  // Fix null message
      "data", data != null ? data : "",                        // Fix null data
      "timestamp", Instant.now().toString()
    );
  }
}