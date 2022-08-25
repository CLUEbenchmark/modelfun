package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 记录每条规则下，每条语料对应的标签
 *
 * @version 1.0
 * @date 2022/4/15 12:18
 */
@Data
@TableName(value = "mf_rule_result")
public class RuleResultPO {

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
   * 测试集中语料ID
   */
  @TableField(value = "sentence_id")
  private Long sentenceId;

  /**
   * 标签集中的标签ID
   */
  @TableField(value = "label_id")
  private Integer labelId;
  /**
   * 是否为展示部分的记录 0：否 1：是
   */
  @TableField(value = "show_data")
  private Integer showData;
  /**
   * 数据类型：4-训练集，5-测试集
   */
  @TableField(value = "data_type")
  private Integer dataType;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_RULE_ID = "rule_id";

  public static final String COL_SENTENCE_ID = "sentence_id";

  public static final String COL_LABEL_ID = "label_id";

  public static final String COL_SHOW_DATA = "show_data";

  public static final String COL_DATA_TYPE = "data_type";

}
