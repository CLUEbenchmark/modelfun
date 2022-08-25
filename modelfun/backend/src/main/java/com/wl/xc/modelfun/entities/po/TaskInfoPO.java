package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 任务信息表
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "mf_task_info")
public class TaskInfoPO {

  /**
   * 任务ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 任务所属用户ID
   */
  @TableField(value = "user_id")
  private Integer userId;

  /**
   * 任务名称
   */
  @TableField(value = "`name`")
  private String name;

  /**
   * 任务领域
   */
  @TableField(value = "`domain`")
  private String domain;

  /**
   * 任务类型
   */
  @TableField(value = "task_type")
  private Integer taskType;

  /**
   * 语言类型
   */
  @TableField(value = "language_type")
  private Integer languageType;

  /**
   * 关键词
   */
  @TableField(value = "keyword")
  private String keyword;

  /**
   * 任务描述
   */
  @TableField(value = "task_desc")
  private String description;

  /**
   * 是否删除
   */
  @TableField(value = "deleted")
  private Boolean deleted;

  /**
   * 创建人
   */
  @TableField(value = "create_people")
  private String createPeople;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  /**
   * 更新人
   */
  @TableField(value = "update_people")
  private String updatePeople;

  /**
   * 更新时间
   */
  @TableField(value = "update_datetime")
  private LocalDateTime updateDatetime;

  public static final String COL_ID = "id";

  public static final String COL_USER_ID = "user_id";

  public static final String COL_NAME = "name";

  public static final String COL_DOMAIN = "domain";

  public static final String COL_TASK_TYPE = "task_type";

  public static final String COL_LANGUAGE_TYPE = "language_type";

  public static final String COL_KEYWORD = "keyword";

  public static final String COL_DESCRIPTION = "description";

  public static final String COL_DELETED = "deleted";

  public static final String COL_CREATE_PEOPLE = "create_people";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_UPDATE_PEOPLE = "update_people";

  public static final String COL_UPDATE_DATETIME = "update_datetime";
}