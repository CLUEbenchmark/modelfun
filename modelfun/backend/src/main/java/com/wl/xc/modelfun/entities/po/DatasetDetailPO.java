package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 数据集中数据的明细，每条记录对应一条数据
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_dataset_detail")
public class DatasetDetailPO {

  /**
   * 自增ID
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
  private Integer dataSetId;

  /**
   * 数据集类型（1：测试集，2：未标注数据集）
   */
  @TableField(value = "file_type")
  private Integer fileType;
  /**
   * 更新时间
   */
  @TableField(value = "update_datetime")
  private LocalDateTime updateDatetime;

  /**
   * 文件地址
   */
  @TableField(value = "file_address")
  private String fileAddress;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_FILE_TYPE = "file_type";

  public static final String COL_FILE_ADDRESS = "file_address";

  public static final String COL_UPDATE_DATETIME = "update_datetime";
}