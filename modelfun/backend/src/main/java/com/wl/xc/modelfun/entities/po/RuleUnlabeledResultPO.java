package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Fan
 * @version 1.0
 * @date 2022.4.15 23:40
 */

/**
 * 未标注数据集经过规则运行后打上的标签
 */
@Data
@TableName(value = "mf_rule_unlabeled_result")
public class RuleUnlabeledResultPO {

  /**
   * 记录ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 规则ID
   */
  @TableField(value = "rule_id")
  private Long ruleId;

  /**
   * 未标注数据集中的语料ID
   */
  @TableField(value = "sentence_id")
  private Long sentenceId;

  /**
   * 该规则下未标注数据集语料的标签
   */
  @TableField(value = "label_id")
  private Integer labelId;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_RULE_ID = "rule_id";

  public static final String COL_SENTENCE_ID = "sentence_id";

  public static final String COL_LABEL_ID = "label_id";
}