package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 规则集成记录表，该表记录的是规则训练的流水记录
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_integration_records")
public class IntegrationRecordsPO {

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
   * 数据集ID
   */
  @TableField(value = "dataset_id")
  private Integer datasetId;

  /**
   * 自动标注结果文件地址
   */
  @TableField(value = "result_file_address")
  private String resultFileAddress;

  /**
   * 标注状态 0-未标注 1-标注中 2-标注成功 3-标注失败
   */
  @TableField(value = "labeled")
  private Integer labeled;

  /**
   * 投票模型地址
   */
  @TableField(value = "vote_model_address")
  private String voteModelAddress;

  /**
   * 集成之后，标签映射的地址
   */
  @TableField(value = "mapping_model_address")
  private String mappingModelAddress;

  /**
   * 集成状态。0：集成中，1：集成成功，2：集成失败
   */
  @TableField(value = "integrate_status")
  private Integer integrateStatus;
  /**
   * 训练集标签类别数
   */
  @TableField(value = "train_label_count")
  private Integer trainLabelCount;
  /**
   * 训练集语料标注量
   */
  @TableField(value = "train_sentence_count")
  private Long trainSentenceCount;
  /**
   * 测试集的准确率
   */
  @TableField(value = "test_accuracy")
  private String testAccuracy;
  /**
   * 测试集的标注结果召回率
   */
  @TableField(value = "test_recall")
  private String testRecall;
  /**
   * 测试集的F1值
   */
  @TableField(value = "test_f1_score")
  private String testF1Score;
  /**
   * 未标注数据集的覆盖率
   */
  @TableField(value = "unlabel_coverage")
  private String unlabelCoverage;

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

  /**
   * 耗时时间
   */
  @TableField(value = "time_cost")
  private Integer timeCost;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_RESULT_FILE_ADDRESS = "result_file_address";

  public static final String COL_LABELED = "labeled";

  public static final String COL_INTEGRATE_STATUS = "integrate_status";

  public static final String COL_TRAIN_LABEL_COUNT = "train_label_count";

  public static final String COL_TRAIN_SENTENCE_COUNT = "train_sentence_count";

  public static final String COL_TEST_ACCURACY = "test_accuracy";

  public static final String COL_TEST_RECALL = "test_recall";

  public static final String COL_TEST_F1_SCORE = "test_f1_score";

  public static final String COL_UNLABEL_COVERAGE = "unlabel_coverage";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_UPDATE_DATETIME = "update_datetime";

  public static final String COL_VOTE_MODEL_ADDRESS = "vote_model_address";

  public static final String COL_MAPPING_MODEL_ADDRESS = "mapping_model_address";

}