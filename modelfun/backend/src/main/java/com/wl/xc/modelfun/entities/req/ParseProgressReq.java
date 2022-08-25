package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/13 17:55
 */
@Data
@Schema(description = "解析进度请求", name = "ParseProgressReq")
public class ParseProgressReq {

  /**
   * 任务ID
   */
  @Schema(description = "任务ID", name = "taskId")
  @NotNull(message = "任务ID不能为空")
  private Long taskId;

  /**
   * 解析请求ID
   */
  @Schema(description = "解析请求ID", name = "requestId")
  @NotBlank(message = "解析请求ID不能为空")
  private String requestId;

}
