package com.view26.ci.plugin.utils;

/**
 * @author aneeshia
 */
public class ClientRequestException extends Exception {
  public ClientRequestException() {
    super();
  }

  public ClientRequestException(String message) {
    super(message);
  }

  public ClientRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
