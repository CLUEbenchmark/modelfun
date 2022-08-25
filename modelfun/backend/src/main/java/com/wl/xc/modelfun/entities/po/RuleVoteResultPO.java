package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 规则最终结果表，存储的是所有规则投票完成之后，最后的语料对应的标签
 *
 * @version 1.0
 * @date 2022/4/15 12:18
 */
@Data
@TableName(value = "mf_rule_vote_result")
public class RuleVoteResultPO {

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
   * 标签集中的标签ID
   */
  @TableField(value = "label_id")
  private Integer labelId;

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_SENTENCE_ID = "sentence_id";

  public static final String COL_LABEL_ID = "label_id";
}