package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 任务规则概览表
 *
 * @version 1.0
 * @date 2022/4/15 12:18
 */
@Data
@TableName(value = "mf_rule_overview")
public class RuleOverviewPO {

  /**
   * 标注规则概览记录ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 准确率
   */
  @TableField(value = "accuracy")
  private String accuracy;

  /**
   * 冲突率
   */
  @TableField(value = "conflict")
  private String conflict;

  /**
   * 覆盖率
   */
  @TableField(value = "coverage")
  private String coverage;

  /**
   * 测试集的覆盖率
   */
  @TableField(value = "test_data_coverage")
  private String testDataCoverage;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  /**
   * 更新时间
   */
  @TableField(value = "update_datetime")
  private LocalDateTime updateDatetime;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_ACCURACY = "accuracy";

  public static final String COL_CONFLICT = "conflict";

  public static final String COL_COVERAGE = "coverage";

  public static final String COL_TEST_DATA_COVERAGE = "test_data_coverage";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_UPDATE_DATETIME = "update_datetime";
}