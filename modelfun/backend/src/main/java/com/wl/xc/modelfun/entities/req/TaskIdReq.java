package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/12 16:46
 */
@Data
@Schema(name = "TaskIdReq", description = "用于任务ID的查询请求对象")
public class TaskIdReq {

  @NotNull(message = "任务ID不能为空")
  @Schema(name = "taskId", description = "任务ID")
  private Long taskId;

}
