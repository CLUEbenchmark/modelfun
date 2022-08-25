package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 和算法对接时数据集的输入参数
 *
 * @version 1.0
 * @date 2022/4/20 16:10
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NerDatasetInput extends DatasetInput {

  @JsonProperty("model_name")
  private String modelName = "macbert";

  @JsonProperty("unlabeled_path")
  private String unlabeledPath;

  private List<String> schemas;

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
