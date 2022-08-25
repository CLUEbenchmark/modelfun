package com.wl.xc.modelfun.commons.constants;

/**
 * @version 1.0
 * @date 2022/4/14 10:32
 */
public class CommonConstant {

  // 登录相关的参数
  /**
   * 登录用户保存在redis中的key的前缀
   */
  public static final String SESSION_PREFIX_NAME = "session:";

  /**
   * 无状态token的前缀
   */
  public static final String TOKEN_PREFIX = "Bearer ";

  // http相关的参数
  /**
   * 默认的保持连接时间
   */
  public static final long DEFAULT_KEEP_ALIVE_TIME = 60 * 1000;
  /**
   * 最大重试次数
   */
  public static final long MAX_RETRY = 3;
  /**
   * http header中的content-type
   */
  public static final String CONTENT_TYPE = "Content-Type";
  /**
   * 默认的content-type
   */
  public static final String DEFAULT_CONTENT_TYPE = "application/json";
  /**
   * 请求成功的状态码
   */
  public static final int SUCCESS_STATUS = 200;

  public static final String DEFAULT_TIME_ZONE = "GMT+8";

  public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

  public static final String OSS_DELIMITER = "/";

  public static final String EXPERT_UPLOAD_TYPE = "uploadType";

  public static final String WS_TOKEN_PAYLOAD = "token_payload";

  public static final String SESSION_UID = "UID";

  public static final String UPDATE_TIME = "UPDATE";

  public static final String SESSION_USER_ID = "USERID";

  public static final String RULE_DELETE = "rule_delete";

  public static final String OSS_TEMP_TAG = "tempFile";
  /**
   * 超时时间，2小时
   */
  public static final long SESSION_TIME_OUT = 60 * 60 * 2;
}
