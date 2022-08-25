package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/14 15:54
 */
@Data
@TableName(value = "mf_ner_auto_label_train")
public class NerAutoLabelTrainPO {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  @TableField(value = "task_id")
  private Long taskId;

  @TableField(value = "data_id")
  private Long dataId;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATA_ID = "data_id";
}