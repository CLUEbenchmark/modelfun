package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/10 17:14
 */
@Data
@Schema(name = "NerTrainLabelResultVO", description = "NER训练标签结果")
public class NerTrainLabelResultVO {

  @Schema(name = "id", description = "NER训练标签结果")
  private Long id;
  @Schema(name = "trainRecordId", description = "训练记录")
  private Long trainRecordId;
  @Schema(name = "labelDes", description = "类别")
  private String labelDes;
  @Schema(name = "trainPrecision", description = "精确率")
  private String trainPrecision;
  @Schema(name = "recall", description = "召回率")
  private String recall;

  /**
   * 样本数
   */
  @Schema(name = "samples", description = "样本数")
  private Integer samples;

  /**
   * 预测错误数
   */
  @Schema(name = "errorCount", description = "预测错误数")
  private Integer errorCount;
}
