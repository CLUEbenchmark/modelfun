package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 数据集信息表，对应任务下的数据集信息
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_dataset_info")
public class DatasetInfoPO {

  /**
   * 自增ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  /**
   * 集合名称
   */
  @TableField(value = "`name`")
  private String name;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 集合文件地址
   */
  @TableField(value = "dataset_address")
  private String datasetAddress;

  /**
   * 集合描述
   */
  @TableField(value = "dataset_desc")
  private String datasetDesc;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  /**
   * 创建人
   */
  @TableField(value = "create_poeple")
  private String createPoeple;

  /**
   * 更新时间
   */
  @TableField(value = "update_datetime")
  private LocalDateTime updateDatetime;

  /**
   * 更新人
   */
  @TableField(value = "update_people")
  private String updatePeople;

  /**
   * 是否删除
   */
  @TableField(value = "deleted")
  private Boolean deleted;

  public static final String COL_ID = "id";

  public static final String COL_NAME = "name";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ADDRESS = "dataset_address";

  public static final String COL_DATASET_DESC = "dataset_desc";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_CREATE_POEPLE = "create_poeple";

  public static final String COL_UPDATE_DATETIME = "update_datetime";

  public static final String COL_UPDATE_PEOPLE = "update_people";

  public static final String COL_DELETED = "deleted";
}