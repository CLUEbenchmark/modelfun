package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/26 18:00
 */
@Data
public class NerOneClickDTO {

  /**
   * 未标注集路径
   */
  @JsonProperty("unlabeled_path")
  private String unlabeledPath;
  /**
   * 测试集路径
   */
  @JsonProperty("test_path")
  private String testPath;
  /**
   * 样例，格式为['任务','时间']，即要抽取的标签列表
   */
  @JsonProperty("schemas")
  private List<String> schemas;
  /**
   * 用于微调数据，格式与test相同，需要有每一个目标的例子
   */
  @JsonProperty("tune_path")
  private String tunePath;
  /**
   * 模型类别，当前支持uie
   */
  @JsonProperty("model_name")
  private String modelName;
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
  private String callback;
}
