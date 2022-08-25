package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/9 19:20
 */
@Data
@TableName(value = "mf_train_label_sentence_info")
public class TrainLabelSentenceInfoPO {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField(value = "train_record_id")
  private Long trainRecordId;

  @TableField(value = "data_id")
  private Long dataId;

  @TableField(value = "sentence")
  private String sentence;

  @TableField(value = "label_actual")
  private String labelActual;

  @TableField(value = "label_predict")
  private String labelPredict;

  public static final String COL_ID = "id";

  public static final String COL_TRAIN_RECORD_ID = "train_record_id";

  public static final String COL_DATA_ID = "data_id";

  public static final String COL_SENTENCE = "sentence";

  public static final String COL_LABEL_ACTUAL = "label_actual";

  public static final String COL_LABEL_PREDICT = "label_predict";
}