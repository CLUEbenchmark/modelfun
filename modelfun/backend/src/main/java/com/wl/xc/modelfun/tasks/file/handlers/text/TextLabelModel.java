package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/16 18:27
 */
@Data
public class TextLabelModel {

  @ExcelProperty("标签id")
  @JsonProperty("label")
  private Integer labelId;

  @ExcelProperty("标签名称")
  @JsonProperty("label_des")
  private String labelDesc;
}
