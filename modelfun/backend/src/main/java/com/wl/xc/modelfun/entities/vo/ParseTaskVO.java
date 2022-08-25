package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 解析任务视图类
 *
 * @version 1.0
 * @date 2022/5/5 14:37
 */
@Data
public class ParseTaskVO {

  /**
   * 是否存在解析任务
   */
  @Schema(name = "exitParseTask", description = "是否存在专家解析任务")
  private Boolean exitParseTask;
  /**
   * 解析任务ID
   */
  @Schema(name = "requestId", description = "解析任务ID")
  private String requestId;
}
