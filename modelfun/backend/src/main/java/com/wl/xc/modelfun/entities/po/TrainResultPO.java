package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 模型训练结果表
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_train_result")
public class TrainResultPO {

  /**
   * 模型训练结果记录ID
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
   * 训练集数量
   */
  @TableField(value = "train_count")
  private Integer trainCount;

  /**
   * 训练记录ID
   */
  @TableField(value = "train_record_id")
  private Long trainRecordId;

  /**
   * 标注规则数量
   */
  @TableField(value = "rule_count")
  private Integer ruleCount;

  /**
   * 标签类别数量
   */
  @TableField(value = "label_type_count")
  private Integer labelTypeCount;

  /**
   * 覆盖率
   */
  @TableField(value = "coverage")
  private String coverage;

  /**
   * 准确率
   */
  @TableField(value = "accuracy")
  private String accuracy;

  /**
   * 精准率
   */
  @TableField(value = "train_precision")
  private String trainPrecision;

  /**
   * 召回率
   */
  @TableField(value = "recall")
  private String recall;

  /**
   * f1 score
   */
  @TableField(value = "f1_score")
  private String f1Score;

  /**
   * 模型类型(1：投票模型，2：BERT)
   */
  @TableField(value = "module_type")
  private Integer moduleType;

  /**
   * 文件地址
   */
  @TableField(value = "file_address")
  private String fileAddress;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  @TableField(value = "confusion_mx")
  private String confusionMx;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_TRAIN_COUNT = "train_count";

  public static final String COL_TRAIN_RECORD_ID = "train_record_id";

  public static final String COL_RULE_COUNT = "rule_count";

  public static final String COL_LABEL_TYPE_COUNT = "label_type_count";

  public static final String COL_COVERAGE = "coverage";

  public static final String COL_ACCURACY = "accuracy";

  public static final String COL_TRAIN_PRECISION = "train_precision";

  public static final String COL_RECALL = "recall";

  public static final String COL_F1_SCORE = "f1_score";

  public static final String COL_MODULE_TYPE = "module_type";

  public static final String COL_FILE_ADDRESS = "file_address";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_CONFUSION_MX = "confusion_mx";

}