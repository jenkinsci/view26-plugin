package com.view26.ci.plugin.utils;

/**
 * @author aneeshia
 */
public class ResponseEntity {
  private final String body;
  private final Integer statusCode;

  public ResponseEntity(String body, Integer statusCode) {
    this.body = body;
    this.statusCode = statusCode;
  }

  public String getBody() {
    return body;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  @Override public String toString() {
    return "ResponseEntity{" +
      "body='" + body + '\'' +
      ", statusCode=" + statusCode +
      '}';
  }
}
