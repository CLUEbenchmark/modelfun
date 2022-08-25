package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/5/24 15:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName(value = "mf_ner_data_label")
public class NerDataLabelWithDesPO extends NerDataLabelPO {

  @TableField(value = "label_desc")
  private String labelDesc;

  public static final String COL_LABEL_DESC = "labelDesc";
}
