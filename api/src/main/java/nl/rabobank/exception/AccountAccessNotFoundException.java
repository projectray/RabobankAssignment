package nl.rabobank.exception;

public class AccountAccessNotFoundException extends RuntimeException {
  public AccountAccessNotFoundException(String message) {
    super(message);
  }
}
