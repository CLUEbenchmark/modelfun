package com.wl.xc.modelfun.entities.model;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import java.io.File;
import lombok.Data;

/**
 * 文件解析后的结果
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 18:43
 */
@Data
public class DataSetParseResult {

  private int length = 0;
  private int labelCount = 0;
  private int sentenceCount = 0;
  private String fileName;
  private File file;
  private DatasetType setType;
}
