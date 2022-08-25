package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 规则集成标签结果表
 *
 * @author Fan
 * @version 1.0
 * @date 2022/4/29 23:13
 */
@Data
@TableName(value = "mf_integrate_label_result")
public class IntegrateLabelResultPO {

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
  private Long datasetId;

  /**
   * 语料ID
   */
  @TableField(value = "sentence_id")
  private Long sentenceId;

  /**
   * 语料内容
   */
  @TableField(value = "sentence")
  private String sentence;

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
   * 操作状态，预留字段
   */
  @TableField(value = "op_status")
  private Integer opStatus;

  /**
   * 是否使用，预留状态
   */
  @TableField(value = "used")
  private Byte used;

  /**
   * 数据类型，1：高置信；2：待审核
   */
  @TableField(value = "data_type")
  private Integer dataType;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_SENTENCE_ID = "sentence_id";

  public static final String COL_SENTENCE = "sentence";

  public static final String COL_LABEL_ID = "label_id";

  public static final String COL_LABEL_DES = "label_des";

  public static final String COL_OP_STATUS = "op_status";

  public static final String COL_USED = "used";

  public static final String COL_DATA_TYPE = "data_type";

}