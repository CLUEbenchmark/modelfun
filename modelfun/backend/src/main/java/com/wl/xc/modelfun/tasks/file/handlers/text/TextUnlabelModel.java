package com.wl.xc.modelfun.tasks.file.handlers.text;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/17 14:10
 */
@Data
public class TextUnlabelModel {

  @JsonProperty("sentence")
  @ExcelProperty("问法语料")
  private String sentence;
}
