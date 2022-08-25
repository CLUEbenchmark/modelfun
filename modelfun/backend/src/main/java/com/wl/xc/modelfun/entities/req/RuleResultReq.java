package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/28 9:18
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class RuleResultReq extends TaskIdPageReq {

  @NotNull(message = "任务ID不能为空")
  @Schema(name = "ruleId", description = "任务ID")
  private Long ruleId;
}
