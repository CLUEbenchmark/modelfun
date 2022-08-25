package com.wl.xc.modelfun.entities.vo;

import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/28 11:47
 */
@Data
public class RuleMistakeVO {

  /**
   * 数据ID
   */
  private Long dataId;

  /**
   * 语料
   */
  private String sentence;
  /**
   * 原始标签
   */
  private String originLabel;
  /**
   * 标注标签
   */
  private String labeledLabel;
}
