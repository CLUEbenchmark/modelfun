package com.wl.xc.modelfun.entities.dto;

import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/26 13:53
 */
@Data
public class CallbackEventDTO {

  /**
   * 任务ID
   */
  private Long taskId;

  private Long recordId;
  /**
   * 业务类型
   */
  private String topic;
  /**
   * 执行时间，精确到毫秒的时间戳
   */
  private long executeTime;
  /**
   * 内容，json格式存储
   */
  private String body;
}
