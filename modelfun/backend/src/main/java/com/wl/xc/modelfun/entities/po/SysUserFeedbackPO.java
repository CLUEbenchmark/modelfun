package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/7/11 18:37
 */
@Data
@TableName(value = "sys_user_feedback")
public class SysUserFeedbackPO {

  /**
   * 数据ID
   */
  @TableId(value = "id", type = IdType.AUTO)
  private Long id;

  /**
   * 用户ID
   */
  @TableField(value = "user_id")
  private Integer userId;

  @TableField(value = "question")
  private String question;

  /**
   * 附件地址1
   */
  @TableField(value = "appendix1")
  private String appendix1;

  /**
   * 附件地址2
   */
  @TableField(value = "appendix2")
  private String appendix2;

  /**
   * 附件地址3
   */
  @TableField(value = "appendix3")
  private String appendix3;

  /**
   * 附件地址4
   */
  @TableField(value = "appendix4")
  private String appendix4;

  /**
   * 问题状态：1- 待处理 2- 已回复 3- 已忽略
   */
  @TableField(value = "question_status")
  private Integer questionStatus;

  /**
   * 回复
   */
  @TableField(value = "answer")
  private String answer;

  /**
   * 回复人员ID
   */
  @TableField(value = "answer_people")
  private Integer answerPeople;

  /**
   * 备注
   */
  @TableField(value = "remark")
  private String remark;

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

  public static final String COL_ID = "id";

  public static final String COL_USER_ID = "user_id";

  public static final String COL_QUESTION = "question";

  public static final String COL_APPENDIX1 = "appendix1";

  public static final String COL_APPENDIX2 = "appendix2";

  public static final String COL_APPENDIX3 = "appendix3";

  public static final String COL_APPENDIX4 = "appendix4";

  public static final String COL_QUESTION_STATUS = "question_status";

  public static final String COL_ANSWER = "answer";

  public static final String COL_ANSWER_PEOPLE = "answer_people";

  public static final String COL_REMARK = "remark";

  public static final String COL_CREATE_DATETIME = "create_datetime";

  public static final String COL_UPDATE_DATETIME = "update_datetime";
}