package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 模型训练记录表
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_train_records")
public class TrainRecordsPO {

  /**
   * 模型训练操作记录ID
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
   * 数据版本
   */
  @TableField(value = "data_version")
  private String dataVersion;

  /**
   * 训练结果(0:训练中，1：训练完成，-1：取消训练, 2:训练失败)
   */
  @TableField(value = "train_status")
  private Integer trainStatus;

  /**
   * 训练关联文件地址
   */
  @TableField(value = "train_file")
  private String trainFile;

  /**
   * 模型类型：1-LR；2-BERT
   */
  @TableField(value = "model_type")
  private Integer modelType;

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

  @TableField(value = "rule_count")
  private Integer ruleCount;

  @TableField(value = "label_count")
  private Integer labelCount;

  @TableField(value = "train_count")
  private Integer trainCount;

  @TableField(value = "label_array")
  private String labelArray;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_DATA_VERSION = "data_version";

  public static final String COL_TRAIN_STATUS = "train_status";

  public static final String COL_TRAIN_FILE = "train_file";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_UPDATE_DATETIME = "update_datetime";

  public static final String COL_MODEL_TYPE = "model_type";

  public static final String COL_RULE_COUNT = "rule_count";

  public static final String COL_LABEL_COUNT = "label_count";

  public static final String COL_TRAIN_COUNT = "train_count";
}