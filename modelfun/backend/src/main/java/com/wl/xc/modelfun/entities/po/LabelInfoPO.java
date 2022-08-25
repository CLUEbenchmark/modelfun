package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 标签集表，对应数据集中的标签文件内容
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_label_info")
public class LabelInfoPO {

  /**
   * 标签记录ID
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
   * 标签ID
   */
  @TableField(value = "label_id")
  private Integer labelId;

  /**
   * 标签描述
   */
  @TableField(value = "label_desc")
  private String labelDesc;

  /**
   * 高频词
   */
  @TableField(value = "high_frequency_word")
  private String hfWord;

  /**
   * 标签说明
   */
  @TableField(value = "description")
  private String description;

  /**
   * 示例说明
   */
  @TableField(value = "example")
  private String example;

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

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_DATASET_ID = "dataset_id";

  public static final String COL_LABEL_ID = "label_id";

  public static final String COL_LABEL_DESC = "label_desc";

  public static final String COL_HF_WORD = "high_frequency_word";

  public static final String COL_DESCRIPTION = "description";

  public static final String COL_EXAMPLE = "example";

  public static final String COL_UPDATE_DATETIME = "update_datetime";

  public static final String COL_UPDATE_PEOPLE = "update_people";
}