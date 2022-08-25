package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/5/10 15:38
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LabelModelInput extends DatasetInput {

  /**
   * 未标注的数据集的规则打标签的结果，数组内的每个数组表示按照文件行顺序的语料。
   */
  @JsonProperty("train_label_matrix")
  private String trainLabelMatrix;
  /**
   * 测试集的规则打标签的结果，数组内的每个数组表示按照文件行顺序的语料。
   */
  @JsonProperty("test_label_matrix")
  private String testLabelMatrix;

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

}
