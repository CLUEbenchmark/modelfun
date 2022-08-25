package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/23 14:34
 */
@Data
public class BaseCallbackDTO {

  /**
   * 错误明细，如果发送错误才有，如果没有错误，则为空
   */
  @JsonProperty("detail")
  private String detail;

  /**
   * 任务ID
   */
  @JsonProperty("task_id")
  private Long taskId;

  /**
   * 记录ID
   */
  @JsonProperty("record_id")
  private Long recordId;
  /**
   * 是否成功
   */
  @JsonProperty("state")
  private Boolean state;
}
