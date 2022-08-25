package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/5/17 17:12
 */
@NoArgsConstructor
@Data
public class GptDTO {

  /**
   * 需要打标的数据的文件地址
   */
  @JsonProperty("texts")
  private String texts;
  /**
   * 示例
   */
  @JsonProperty("examples")
  private List<List<String>> examples;
  /**
   * 标签集合
   */
  @JsonProperty("labels")
  private List<String> labels;
  /**
   * 模型类型：当前支持 'gpt3', 'sim', 'roberta'
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
  @JsonProperty("callback")
  private String callback;
}
