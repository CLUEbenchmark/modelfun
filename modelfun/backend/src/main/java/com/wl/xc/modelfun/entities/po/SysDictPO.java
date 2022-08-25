package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 字典表
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "sys_dict")
public class SysDictPO {

  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  /**
   * 字典映射的key
   */
  @TableField(value = "map_key")
  private String mapKey;

  /**
   * 字典映射的value
   */
  @TableField(value = "map_value")
  private String mapValue;

  /**
   * 字典映射分组
   */
  @TableField(value = "map_group")
  private String mapGroup;

  /**
   * 描述
   */
  @TableField(value = "map_desc")
  private String mapDesc;

  /**
   * 排序
   */
  @TableField(value = "map_sort")
  private Integer mapSort;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  /**
   * 创建人
   */
  @TableField(value = "create_people")
  private String createPeople;

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

  public static final String COL_MAP_KEY = "map_key";

  public static final String COL_MAP_VALUE = "map_value";

  public static final String COL_MAP_GROUP = "map_group";

  public static final String COL_MAP_DESC = "map_desc";

  public static final String COL_MAP_SORT = "map_sort";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_CREATE_PEOPLE = "create_people";

  public static final String COL_UPDATE_DATETIME = "update_datetime";

  public static final String COL_UPDATE_PEOPLE = "update_people";
}