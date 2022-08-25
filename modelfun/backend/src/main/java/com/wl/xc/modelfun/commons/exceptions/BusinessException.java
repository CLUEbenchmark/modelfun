package com.wl.xc.modelfun.commons.exceptions;

/**
 * 业务异常，用于定义项目内部的异常，建议其他业务异常都继承该类。
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/3/10 14:13
 */
public class BusinessException extends RuntimeException {

  private static final long serialVersionUID = -4016280362994126002L;

  protected static final int DEFAULT_CODE = 500;

  /**
   * 业务异常错误码，默认500
   */
  private int code = DEFAULT_CODE;

  public BusinessException() {
  }

  public BusinessException(String message) {
    super(message);
  }

  public BusinessException(String message, int code) {
    super(message);
    this.code = code;
  }

  public BusinessException(String message, int code, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public BusinessException(Throwable cause) {
    super(cause);
  }

  public BusinessException(String message, int code, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }
}
