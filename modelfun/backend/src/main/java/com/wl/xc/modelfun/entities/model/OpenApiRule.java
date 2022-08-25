package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用外部接口规则
 *
 * @version 1.0
 * @date 2022/4/15 15:29
 */
@NoArgsConstructor
@Data
public class OpenApiRule {

  private Long taskId;

  /**
   * 服务器地址和端口号
   */
  @JsonProperty("host")
  private String host;
  /**
   * requestBody
   */
  @JsonProperty("requestBody")
  private String requestBody;

  /**
   * 批次大小,默认为500
   */
  @JsonProperty("batchSize")
  private Integer batchSize = 500;
}
