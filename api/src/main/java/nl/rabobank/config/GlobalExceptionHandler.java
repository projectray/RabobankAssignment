package nl.rabobank.config;


import lombok.extern.slf4j.Slf4j;
import nl.rabobank.exception.AccountAccessNotFoundException;
import nl.rabobank.exception.BadRequestException;
import nl.rabobank.exception.PoaExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(AccountAccessNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleAccountAccessNotFound(AccountAccessNotFoundException ex) {
    log.warn("Account access not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage());
  }

  @ExceptionHandler(PoaExistException.class)
  public ResponseEntity<Map<String, String>> handlePoAExist(PoaExistException ex) {
    log.warn("Power of Attorney already exists: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationError(MethodArgumentNotValidException ex) {
    String errors = ex.getFieldErrors().stream()
      .map(FieldError::getDefaultMessage)
      .collect(Collectors.joining("; "));

    log.warn("Validation errors: {}", errors);

    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUsernameNotFound(UsernameNotFoundException ex) {
    log.warn("User not found: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Bad credentials", ex.getMessage());
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error occurred");
  }

  private ResponseEntity<Map<String, String>> buildErrorResponse(HttpStatus status, String error, String message) {
    Map<String, String> errorBody = Map.of(
      "error", error,
      "message", message
    );
    return new ResponseEntity<>(errorBody, status);
  }
}
