package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算法异步任务回调
 *
 * @version 1.0
 * @date 2022/6/21 14:57
 */
@NoArgsConstructor
@Data
public class AsyncRspDTO {

  /**
   * 消息
   */
  @JsonProperty("message")
  private String message;
  /**
   * 超时时间
   */
  @JsonProperty("timeout")
  private Long timeout;
}
