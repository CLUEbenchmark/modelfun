package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/12 13:29
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "ModelTrainReq", description = "模型训练结果分页查询请求对象")
public class ModelTrainReq extends BasePageQueryDTO {

  @NotNull(message = "任务ID不能为空")
  @Schema(name = "taskId", description = "任务ID")
  private Long taskId;

}
