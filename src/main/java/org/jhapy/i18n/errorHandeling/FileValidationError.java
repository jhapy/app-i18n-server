package org.jhapy.i18n.errorHandeling;

public class FileValidationError extends RuntimeException {
  public FileValidationError(String message) {
    super(message);
  }
}
