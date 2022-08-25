package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 集成结果表：Integration_result
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_integration_result")
public class IntegrationResultPO {

  /**
   * 集成结果记录ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 数据集ID
   */
  @TableField(value = "dataset_id")
  private Integer datasetId;

  /**
   * 数据集成记录ID
   */
  @TableField(value = "integration_id")
  private Long integrationId;

  /**
   * 规则ID
   */
  @TableField(value = "rule_id")
  private Long ruleId;

  /**
   * 标签ID
   */
  @TableField(value = "label_id")
  private Integer labelId;

  /**
   * 标签描述
   */
  @TableField(value = "label_des")
  private String labelDes;

  /**
   * 准确率
   */
  @TableField(value = "accuracy")
  private String accuracy;

  /**
   * 覆盖率
   */
  @TableField(value = "coverage")
  private String coverage;

  /**
   * 重复率
   */
  @TableField(value = "`repeat`")
  private String repeat;

  /**
   * 冲突率
   */
  @TableField(value = "conflict")
  private String conflict;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_INTEGRATION_ID = "integration_id";

  public static final String COL_RULE_ID = "rule_id";

  public static final String COL_LABEL_ID = "label_id";

  public static final String COL_LABEL_DES = "label_des";

  public static final String COL_ACCURACY = "accuracy";

  public static final String COL_COVERAGE = "coverage";

  public static final String COL_REPEAT = "repeat";

  public static final String COL_CONFLICT = "conflict";

  public static final String COL_CREATE_DATETIME = "create_datetime";
}