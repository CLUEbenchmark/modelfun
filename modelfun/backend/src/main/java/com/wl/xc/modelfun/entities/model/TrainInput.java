package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * （抄自cohere）
 *
 * @version 1.0
 * @date 2022/5/10 15:38
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TrainInput extends DatasetInput {

  /**
   * 任务ID
   */
  @JsonProperty("task_id")
  private Long taskId;

  /**
   * 记录ID
   */
  @JsonProperty("record_id")
  private Long recordId;

  /**
   * 回调地址
   */
  @JsonProperty("callback")
  private String callback;

  /**
   * 自动标注生成的训练集的标签结果，没有标的数据为-1
   */
  @JsonProperty("train_label")
  private String trainLabel;
  /**
   * 训练集数据
   */
  @JsonProperty("labeled_path")
  private String labeledPath;

  /**
   * 测试集的标签结果，非必传
   */
  @JsonProperty("test_label")
  private String testLabel;

  /**
   * 测试集数据自动标注结果
   */
  @JsonProperty("label_model_prediction")
  private String labelModelPrediction;
}
