package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/5/25 18:37
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName(value = "mf_ner_auto_label_result")
public class NerAutoLabelAndSenPO extends NerAutoLabelResultPO {

  /**
   * 语料
   */
  @TableField(value = "sentence")
  private String sentence;

  public static final String COL_SENTENCE = "sentence";

}