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
@TableName(value = "mf_ner_train_label_result")
public class NerTrainLabelResultPO {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField(value = "train_record_id")
  private Long trainRecordId;

  @TableField(value = "label_des")
  private String labelDes;

  @TableField(value = "train_precision")
  private String trainPrecision;

  @TableField(value = "recall")
  private String recall;

  /**
   * 样本数
   */
  @TableField(value = "samples")
  private Integer samples;

  /**
   * 预测错误数
   */
  @TableField(value = "error_count")
  private Integer errorCount;

  public static final String COL_ID = "id";

  public static final String COL_TRAIN_RECORD_ID = "train_record_id";

  public static final String COL_LABEL_DES = "label_des";

  public static final String COL_TRAIN_PRECISION = "train_precision";

  public static final String COL_RECALL = "recall";

  public static final String COL_SAMPLES = "samples";

  public static final String COL_ERROR_COUNT = "error_count";
}