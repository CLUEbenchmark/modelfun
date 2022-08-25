package com.wl.xc.modelfun.entities.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/7/11 18:37
 */
@Data
public class UserFeedbackReq {

  /**
   * 数据ID
   */
  @Schema(name = "recordId", description = "训练记录ID")
  @JsonProperty("id")
  private Long id;

  /**
   * 用户ID
   */
  @JsonProperty("userId")
  private Integer userId;

  @JsonProperty("question")
  private String question;

  /**
   * 附件地址1
   */
  @JsonProperty("appendix1")
  private String appendix1;

  /**
   * 附件地址2
   */
  @JsonProperty("appendix2")
  private String appendix2;

  /**
   * 附件地址3
   */
  @JsonProperty("appendix3")
  private String appendix3;

  /**
   * 附件地址4
   */
  @JsonProperty("appendix4")
  private String appendix4;

  /**
   * 问题状态：1- 待处理 2- 已回复 3- 已忽略
   */
  @JsonProperty("questionStatus")
  private Integer questionStatus;

  /**
   * 回复
   */
  @JsonProperty("answer")
  private String answer;

  /**
   * 回复人员ID
   */
  @JsonProperty("answerPeople")
  private Integer answerPeople;

  /**
   * 备注
   */
  @JsonProperty("remark")
  private String remark;

  /**
   * 创建时间
   */
  @JsonProperty("createDatetime")
  private LocalDateTime createDatetime;

  /**
   * 更新时间
   */
  @JsonProperty("updateDatetime")
  private LocalDateTime updateDatetime;

}
