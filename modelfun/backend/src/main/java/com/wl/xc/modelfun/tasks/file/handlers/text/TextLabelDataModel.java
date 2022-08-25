package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/6/17 14:41
 */
@NoArgsConstructor
@Data
@JsonInclude(Include.NON_NULL)
public class TextLabelDataModel {

  /**
   * 语料ID
   */
  @ExcelIgnore
  private Long id;
  /**
   * 语料
   */
  @ExcelProperty("问法语料")
  @JsonProperty("sentence")
  private String sentence;
  /**
   * 标签描述
   */
  @ExcelProperty("标签名称")
  @JsonProperty("label_des")
  private String labelDes;
  /**
   * 标签ID
   */
  @ExcelProperty("标签id")
  @JsonProperty("label")
  private Integer label;
}
