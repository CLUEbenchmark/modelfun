package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/6/21 14:50
 */
@NoArgsConstructor
@Data
public class FewShotInput {


  /**
   * 训练集文件路径
   */
  @JsonProperty("train_path")
  private String trainPath;
  /**
   * 未标注集文件路径
   */
  @JsonProperty("unlabeled_path")
  private String unlabeledPath;
  /**
   * 测试集文件路径
   */
  @JsonProperty("test_path")
  private String testPath;
  /**
   * 验证集文件路径
   */
  @JsonProperty("val_path")
  private String valPath;
  /**
   * 标签数量
   */
  @JsonProperty("num_class")
  private Long numClass;
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
