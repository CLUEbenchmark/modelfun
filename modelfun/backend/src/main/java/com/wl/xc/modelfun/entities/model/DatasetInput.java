package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 和算法对接时数据集的输入参数
 *
 * @version 1.0
 * @date 2022/4/20 16:10
 */
@NoArgsConstructor
@Data
public class DatasetInput {

  /**
   * 项目名称
   */
  @JsonProperty("name")
  private String name;
  /**
   * 未标注数据集的oss地址
   */
  @JsonProperty("train_path")
  private String trainPath;
  /**
   * 2022年5月6日16:39:52新增，用于测试集高频词
   */
  @JsonProperty("val_path")
  private String valPath;
  /**
   * 测试集的oss地址
   */
  @JsonProperty("test_path")
  private String testPath;
  /**
   * 任务领域
   */
  @JsonProperty("domain_type")
  private String domainType;
  /**
   * 任务类型
   */
  @JsonProperty("task_type")
  private String taskType;

  /**
   * 关键词
   */
  @JsonProperty("key_words")
  private String keywords;

  /**
   * 任务描述
   */
  @JsonProperty("description")
  private String description;
  /**
   * 标签类别数量
   */
  @JsonProperty("num_class")
  private Long numClass;

  @JsonProperty("val_label_matrix")
  private String valLabelMatrix = "";

}
