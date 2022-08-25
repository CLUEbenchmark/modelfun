package com.wl.xc.modelfun.entities.model;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import lombok.Data;

/**
 * 预测详情
 *
 * @version 1.0
 * @date 2022/7/5 15:14
 */
@Data
public class PredictDetail {

  @ExcelIgnore
  private Long id;

  /**
   * 样本内容
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ExcelProperty(value = "样本内容")
  private String sentence;
  /**
   * 实际类别
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ContentStyle
  @ExcelProperty(value = "实际类别")
  private String actual;
  /**
   * 预测类别
   */
  @HeadStyle(fillPatternType = FillPatternTypeEnum.NO_FILL)
  @ExcelProperty(value = "预测类别")
  private String predict;

}
