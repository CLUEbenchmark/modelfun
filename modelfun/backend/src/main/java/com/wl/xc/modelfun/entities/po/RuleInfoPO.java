package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * modelfun标注规则表
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_rule_info")
public class RuleInfoPO {

  /**
   * 规则ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

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
   * 规则类型（1：模式匹配，2：专家知识，3：数据库，4：外部api）
   */
  @TableField(value = "rule_type")
  private Integer ruleType;

  /**
   * 规则名称
   */
  @TableField(value = "rule_name")
  private String ruleName;

  /**
   * 标签ID
   */
  @TableField(value = "label")
  private Integer label;

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
   * 冲突
   */
  @TableField(value = "conflict")
  private String conflict;

  /**
   * 覆盖率
   */
  @TableField(value = "coverage")
  private String coverage;

  /**
   * 对应未标注数据集的覆盖率
   */
  @TableField(value = "unlabeled_coverage")
  private String unlabeledCoverage;

  /**
   * 重叠率，面向该条标注规则标记的语料，该条标注规则标注的结果与其他标注规则标注的结果产生重复的语料
   */
  @TableField(value = "overlap")
  private String overlap;

  /**
   * 规则是否运行完成
   */
  @TableField(value = "completed")
  private Integer completed;

  /**
   * 规则声明描述
   */
  @TableField(value = "metadata")
  private String metadata;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  /**
   * 规则开始创建的时间
   */
  @TableField(value = "create_start_time")
  private LocalDateTime createStartTime;

  /**
   * 规则创建结束的时间
   */
  @TableField(value = "create_end_time")
  private LocalDateTime createEndTime;

  /**
   * 更新时间
   */
  @TableField(value = "update_datetime")
  private LocalDateTime updateDatetime;

  /**
   * 规则更新开始的时间
   */
  @TableField(value = "update_start_time")
  private LocalDateTime updateStartTime;

  /**
   * 规则更新结束的时间
   */
  @TableField(value = "update_end_time")
  private LocalDateTime updateEndTime;

  /**
   * 是否是平台自动生成的规则 true：是 false：否
   */
  @TableField(value = "auto_generated")
  private Boolean autoGenerated;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_RULE_TYPE = "rule_type";

  public static final String COL_RULE_NAME = "rule_name";

  public static final String COL_LABEL = "label";

  public static final String COL_LABEL_DES = "label_des";

  public static final String COL_ACCURACY = "accuracy";

  public static final String COL_CONFLICT = "conflict";

  public static final String COL_COVERAGE = "coverage";

  public static final String COL_UNLABELED_COVERAGE = "unlabeled_coverage";

  public static final String COL_OVERLAP = "overlap";

  public static final String COL_COMPLETED = "completed";

  public static final String COL_METADATA = "metadata";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_CREATE_START_TIME = "create_start_time";

  public static final String COL_CREATE_END_TIME = "create_end_time";

  public static final String COL_UPDATE_DATETIME = "update_datetime";

  public static final String COL_UPDATE_START_TIME = "update_start_time";

  public static final String COL_UPDATE_END_TIME = "update_end_time";

  public static final String COL_AUTO_GENERATED = "auto_generated";

}