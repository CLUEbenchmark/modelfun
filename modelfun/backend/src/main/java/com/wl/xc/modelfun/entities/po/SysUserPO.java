package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户表
 *
 * @version 1.0
 * @date 2022/4/11 16:13
 */
@Data
@TableName(value = "sys_user")
public class SysUserPO {

  /**
   * 自增id
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;

  /**
   * 用户名称
   */
  @TableField(value = "user_name")
  private String userName;

  /**
   * 用户手机号
   */
  @TableField(value = "user_phone")
  private String userPhone;

  /**
   * 用户密码
   */
  @TableField(value = "user_password")
  private String userPassword;

  /**
   * 创建时间
   */
  @TableField(value = "create_datetime")
  private LocalDateTime createDatetime;

  /**
   * 修改时间
   */
  @TableField(value = "update_datetime")
  private LocalDateTime updateDatetime;

  /**
   * 更新人员
   */
  @TableField(value = "update_people")
  private String updatePeople;

  /**
   * 备注信息
   */
  @TableField(value = "remark")
  private String remark;

  public static final String COL_ID = "id";

  public static final String COL_USER_NAME = "user_name";

  public static final String COL_USER_PHONE = "user_phone";

  public static final String COL_USER_PASSWORD = "user_password";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_UPDATE_DATETIME = "update_datetime";

  public static final String COL_UPDATE_PEOPLE = "update_people";

  public static final String COL_REMARK = "remark";
}