package com.view26.ci.plugin.exception;

/**
 * @author aneeshia
 */
public class SaveSettingException extends Exception {
  private int status;

  public SaveSettingException() {
    super();
  }

  public SaveSettingException(String message) {
    super(message);
  }

  public SaveSettingException(String message, Throwable cause) {
    super(message, cause);
  }

  public SaveSettingException(String message, int status) {
    super(message);
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
