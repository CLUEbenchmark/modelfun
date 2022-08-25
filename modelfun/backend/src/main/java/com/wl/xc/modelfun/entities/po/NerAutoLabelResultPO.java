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
@TableName(value = "mf_ner_auto_label_result")
public class NerAutoLabelResultPO {

  /**
   * ID
   */
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
   * 语料
   */
  @TableField(value = "sentence")
  private String sentence;

  @TableField(value = "relations")
  private String relations;

  @TableField(value = "data_type")
  private Integer dataType;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_SENTENCE_ID = "sentence_id";

  public static final String COL_SENTENCE = "sentence";

  public static final String COL_RELATIONS = "relations";

  public static final String COL_DATA_TYPE = "data_type";
}