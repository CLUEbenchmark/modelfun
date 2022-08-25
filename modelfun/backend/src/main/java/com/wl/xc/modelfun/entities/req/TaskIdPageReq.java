package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/12 16:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "TaskIdPageReq", description = "用于任务ID的分页查询请求对象")
public class TaskIdPageReq extends BasePageQueryDTO {

  @NotNull(message = "任务ID不能为空")
  @Schema(name = "taskId", description = "任务ID")
  private Long taskId;

}
