package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/10 17:28
 */
@Data
@Schema(name = "TextTrainLabelDiffVO", description = "标签结果")
public class TextTrainLabelDiffVO {

  @Schema(name = "sentence", description = "标签结果")
  private String sentence;
  @Schema(name = "actual", description = "实际标签结果")
  private String actual;
  @Schema(name = "predict", description = "错误标签结果")
  private String predict;
}
