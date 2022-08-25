package com.wl.xc.modelfun.commons.exceptions;

/**
 * 一般的参数错误异常
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/3/11 17:26
 */
public class BusinessArgumentException extends BusinessException {

  private static final long serialVersionUID = 1769774723315199248L;

  public BusinessArgumentException() {
  }

  public BusinessArgumentException(String message) {
    super(message, DEFAULT_CODE);
  }

  public BusinessArgumentException(String message, int code) {
    super(message, code);
  }

  public BusinessArgumentException(String message, int code, Throwable cause) {
    super(message, code, cause);
  }

  public BusinessArgumentException(String message, int code, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, code, cause, enableSuppression, writableStackTrace);
  }

  public BusinessArgumentException(String message, Throwable cause) {
    super(message, DEFAULT_CODE, cause);
  }

  public BusinessArgumentException(Throwable cause) {
    super(cause);
  }

  public BusinessArgumentException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, DEFAULT_CODE, cause, enableSuppression, writableStackTrace);
  }
}
