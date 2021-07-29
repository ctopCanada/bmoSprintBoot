package com.bmo.pbt.bmo.pay.the.bil.endpoint.exceptions;

public class RestException extends Exception {

  /** */
  private static final long serialVersionUID = 1L;

  private int httpResponseCode;

  private String httpResponseContent;

  public RestException(String message, int httpResponseCode, String httpResponseContent) {

    super(message);

    this.httpResponseCode = httpResponseCode;

    this.httpResponseContent = httpResponseContent;
  }

  public RestException(Throwable cause, int httpResponseCode, String httpResponseContent) {

    super(cause);

    this.httpResponseCode = httpResponseCode;

    this.httpResponseContent = httpResponseContent;
  }

  public RestException(
      String message, Throwable cause, int httpResponseCode, String httpResponseContent) {

    super(message, cause);

    this.httpResponseCode = httpResponseCode;

    this.httpResponseContent = httpResponseContent;
  }

  public RestException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      int httpResponseCode,
      String httpResponseContent) {

    super(message, cause, enableSuppression, writableStackTrace);

    this.httpResponseCode = httpResponseCode;

    this.httpResponseContent = httpResponseContent;
  }

  public int getHttpResponseCode() {

    return httpResponseCode;
  }

  public void setHttpResponseCode(int httpResponseCode) {

    this.httpResponseCode = httpResponseCode;
  }

  public String getHttpResponseContent() {

    return httpResponseContent;
  }

  public void setHttpResponseContent(String httpResponseContent) {

    this.httpResponseContent = httpResponseContent;
  }
}
