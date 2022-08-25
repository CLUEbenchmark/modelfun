package com.wl.xc.modelfun.entities.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/7/5 15:47
 */
@Data
public class LabelResultDetail {

  /**
   * 类别
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ExcelProperty("类别")
  private String labelDes;
  /**
   * 样本数
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ExcelProperty("样本数")
  private int sample;
  /**
   * 精确率
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ExcelProperty("精确率")
  private String precision;
  /**
   * 召回率
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ExcelProperty("召回率")
  private String recall;
  /**
   * 预测错误样本数
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ExcelProperty("预测错误样本数")
  private int errorCount;
}
