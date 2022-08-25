package com.wl.xc.modelfun.entities.po;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/30 11:24
 */
@Data
@JsonInclude(Include.NON_NULL)
public class SimpleAutoLabelResult {

  /**
   * 数据类型，高置信数据，训练集数据
   */
  @JsonProperty("type")
  private String type;
  /**
   * 语料内容
   */
  @JsonProperty("sentence")
  private String sentence;

  /**
   * 标签ID
   */
  @JsonProperty("label")
  private String label;

  /**
   * 标签描述
   */
  @JsonProperty("label_desc")
  private String labelDes;

}
