package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ner数据集文件中的标签集
 *
 * @version 1.0
 * @date 2022/5/24 14:11
 */
@NoArgsConstructor
@Data
public class NerLabelModel {

  /**
   * 标签描述
   */
  @JsonProperty("ner_des")
  private String nerDes;
  /**
   * 标签ID
   */
  @JsonProperty("ner")
  private String ner;
}
