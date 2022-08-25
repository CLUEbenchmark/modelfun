package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/4/21 9:51
 */
@NoArgsConstructor
@Data
public class LabelFunctionDTO {

  /**
   * lf方法名
   */
  @JsonProperty("name")
  private String name;
  /**
   * 语言类型
   */
  @JsonProperty("language")
  private String language = "python";
  /**
   * lf方法体
   */
  @JsonProperty("content")
  private String content;
  /**
   * 数据集的oss地址,是一个zip文件集，里面包含了3个文件，测试集、标签集、未标注集
   */
  @JsonProperty("data_path")
  private String dataPath;
  /**
   * 测试用例
   */
  @JsonProperty("data_sample")
  private String dataSample;
}
