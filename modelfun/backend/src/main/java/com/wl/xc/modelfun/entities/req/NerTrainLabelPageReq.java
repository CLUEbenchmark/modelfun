package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/6/10 17:34
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "NerTrainLabelPageReq", description = "模型训练数据分析中的标签错误数据查询请求对象")
public class NerTrainLabelPageReq extends TaskIdPageReq {

  @NotNull(message = "记录ID不能为空")
  @Schema(name = "recordId", description = "记录ID")
  private Long recordId;

  @NotNull(message = "训练记录ID不能为空")
  @Schema(name = "trainRecordId", description = "训练记录ID")
  private Long trainRecordId;
}
