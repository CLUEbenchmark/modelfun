package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/16 11:33
 */
@Data
@TableName(value = "mf_gpt_cache")
public class GptCachePO {

  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 数据类型：1-展示的测试集，2-未标注数据集
   */
  @TableField(value = "data_type")
  private Integer dataType;

  /**
   * 数据ID
   */
  @TableField(value = "sentence_id")
  private Long sentenceId;

  /**
   * 规则ID
   */
  @TableField(value = "rule_id")
  private Long ruleId;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATA_TYPE = "data_type";

  public static final String COL_SENTENCE_ID = "sentence_id";

  public static final String COL_RULE_ID = "rule_id";
}