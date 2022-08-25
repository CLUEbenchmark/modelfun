package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/9 18:08
 */
@Data
@TableName(value = "mf_ner_train_label_detail")
public class NerTrainLabelDetailPO {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField(value = "train_label_id")
  private Long trainLabelId;

  /**
   * 语料
   */
  @TableField(value = "data_id")
  private Long dataId;

  public static final String COL_ID = "id";

  public static final String COL_TRAIN_LABEL_ID = "train_label_id";

  public static final String COL_DATA_ID = "data_id";
}