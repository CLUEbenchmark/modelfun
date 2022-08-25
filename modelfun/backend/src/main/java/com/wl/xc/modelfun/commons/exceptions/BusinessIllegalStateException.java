package com.wl.xc.modelfun.commons.exceptions;

/**
 * 不合法的状态异常
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/3/23 16:25
 */
public class BusinessIllegalStateException extends BusinessException {

  private static final long serialVersionUID = -7279725199439098748L;

  public BusinessIllegalStateException() {
    super();
  }

  public BusinessIllegalStateException(String message) {
    super(message, DEFAULT_CODE);
  }

  public BusinessIllegalStateException(String message, Throwable cause) {
    super(message, DEFAULT_CODE, cause);
  }

  public BusinessIllegalStateException(String message, int code) {
    super(message, code);
  }

  public BusinessIllegalStateException(String message, int code, Throwable cause) {
    super(message, code, cause);
  }

  public BusinessIllegalStateException(String message, int code, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, code, cause, enableSuppression, writableStackTrace);
  }

  public BusinessIllegalStateException(Throwable cause) {
    super(cause);
  }

  public BusinessIllegalStateException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, DEFAULT_CODE, cause, enableSuppression, writableStackTrace);
  }
}
