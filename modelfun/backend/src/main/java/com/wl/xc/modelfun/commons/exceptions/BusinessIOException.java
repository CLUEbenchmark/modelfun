package com.wl.xc.modelfun.commons.exceptions;

/**
 * io相关的异常
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/4/1 13:19
 */
public class BusinessIOException extends BusinessException {

  private static final long serialVersionUID = -4367363564046495265L;

  public BusinessIOException() {
  }

  public BusinessIOException(String message) {
    super(message, DEFAULT_CODE);
  }

  public BusinessIOException(String message, Throwable cause) {
    super(message, DEFAULT_CODE, cause);
  }

  public BusinessIOException(String message, int code) {
    super(message, code);
  }

  public BusinessIOException(String message, int code, Throwable cause) {
    super(message, code, cause);
  }

  public BusinessIOException(String message, int code, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, code, cause, enableSuppression, writableStackTrace);
  }

  public BusinessIOException(Throwable cause) {
    super(cause);
  }

  public BusinessIOException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, DEFAULT_CODE, cause, enableSuppression, writableStackTrace);
  }
}
