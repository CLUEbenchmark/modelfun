package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 任务下的专家知识表
 *
 * @version 1.0
 * @date 2022/4/14 15:56
 */
@Data
@TableName(value = "mf_task_expert")
public class TaskExpertPO {

  /**
   * 专家知识ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 任务ID
   */
  @TableField(value = "task_id")
  private Long taskId;

  /**
   * 专家知识文件名称
   */
  @TableField(value = "file_name")
  private String fileName;

  /**
   * 专家知识文件地址
   */
  @TableField(value = "file_address")
  private String fileAddress;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  public static final String COL_ID = "id";

  public static final String COL_TASK_ID = "task_id";

  public static final String COL_FILE_NAME = "file_name";

  public static final String COL_FILE_ADDRESS = "file_address";

  public static final String COL_CREATE_DATETIME = "create_datetime";
}