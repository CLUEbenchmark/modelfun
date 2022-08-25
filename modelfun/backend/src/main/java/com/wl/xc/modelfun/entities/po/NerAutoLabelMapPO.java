package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/25 18:37
 */
@Data
@TableName(value = "mf_ner_auto_label_map")
public class NerAutoLabelMapPO {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 语料ID
   */
  @TableField(value = "sentence_id")
  private Long sentenceId;

  /**
   * 标签ID
   */
  @TableField(value = "label_id")
  private Integer labelId;

  /**
   * 起始位置
   */
  @TableField(value = "start_offset")
  private Integer startOffset;

  /**
   * 结束位置
   */
  @TableField(value = "end_offset")
  private Integer endOffset;

  /**
   * 实体ID
   */
  @TableField(value = "data_id")
  private Long dataId;

  @TableField(value = "data_type")
  private Integer dataType;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_SENTENCE_ID = "sentence_id";

  public static final String COL_LABEL_ID = "label_id";

  public static final String COL_START_OFFSET = "start_offset";

  public static final String COL_END_OFFSET = "end_offset";

  public static final String COL_DATA_ID = "data_id";

  public static final String COL_DATA_TYPE = "data_type";
}