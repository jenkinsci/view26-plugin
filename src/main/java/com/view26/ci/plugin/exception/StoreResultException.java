package com.view26.ci.plugin.exception;

/**
 * @author aneeshia
 */
public class StoreResultException extends Exception {
  public StoreResultException() {
    super();
  }

  public StoreResultException(String message) {
    super(message);
  }

  public StoreResultException(String message, Throwable cause) {
    super(message, cause);
  }

  public StoreResultException(Throwable cause) {
    super(cause);
  }

  protected StoreResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
