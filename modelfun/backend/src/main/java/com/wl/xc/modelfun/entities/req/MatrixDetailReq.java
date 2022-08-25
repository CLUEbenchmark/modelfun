package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/7/11 16:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "MatrixDetailReq", description = "用于获取矩阵详情请求对象")
public class MatrixDetailReq extends TaskIdPageReq {

  /**
   * 模型训练记录ID
   */
  @NotNull(message = "记录ID不能为空")
  @Schema(name = "recordId", description = "训练记录ID")
  private Long recordId;
  /**
   * 预测标签
   */
  @NotBlank(message = "预测标签不能为空")
  @Schema(name = "predict", description = "预测标签")
  private String predict;
  /**
   * 实际标签
   */
  @NotBlank(message = "实际标签不能为空")
  @Schema(name = "actual", description = "实际标签")
  private String actual;
}
