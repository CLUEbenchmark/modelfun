package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/6/10 17:17
 */
@Data
@Schema(name = "NerTrainLabelReq", description = "训练标签请求")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NerTrainLabelReq extends TaskIdReq {

  @NotNull(message = "记录ID不能为空")
  @Schema(name = "recordId", description = "记录ID")
  private Long recordId;
}
