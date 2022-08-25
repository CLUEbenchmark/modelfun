package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/5/6 17:14
 */
@NoArgsConstructor
@Data
public class TrainLabelDTO {


  /**
   * 未标注集打标结果
   */
  @JsonProperty("train_label")
  private String trainLabel;
  /**
   * 高置信数据index
   */
  @JsonProperty("certainty_idx")
  private String certaintyIdx;
  /**
   * 待审核数据index
   */
  @JsonProperty("uncertainty_idx")
  private String uncertaintyIdx;
  /**
   * 验证集数据打标结果
   */
  @JsonProperty("val_label")
  private String valLabel;
  /**
   * 测试集数据打标结果
   */
  @JsonProperty("test_label")
  private String testLabel;
  /**
   * 准确率
   */
  @JsonProperty("accuracy")
  private Double accuracy;
  /**
   * f1
   */
  @JsonProperty("f1")
  private Double f1;
  /**
   * 召回率
   */
  @JsonProperty("recall")
  private Double recall;
  /**
   * 精准率
   */
  @JsonProperty("precision")
  private Double precision;
}
