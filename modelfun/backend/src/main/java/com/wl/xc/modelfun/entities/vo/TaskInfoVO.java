package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/11 16:40
 */
@Data
@Schema(name = "TaskInfoVO", description = "任务信息")
public class TaskInfoVO {

  /**
   * 任务ID
   */
  @Schema(name = "id", description = "任务ID")
  private Long id;

  /**
   * 任务所属用户ID
   */
  @Schema(name = "userId", description = "任务所属用户ID")
  private Integer userId;

  /**
   * 任务名称
   */
  @Schema(name = "name", description = "任务名称")
  private String name;

  /**
   * 任务领域
   */
  @Schema(name = "domain", description = "任务领域")
  private String domain;

  /**
   * 任务类型
   */
  @Schema(name = "taskType", description = "任务类型")
  private Integer taskType;

  /**
   * 语言类型
   */
  @Schema(name = "languageType", description = "语言类型")
  private Integer languageType;

  /**
   * 关键词
   */
  @Schema(name = "keyword", description = "关键词")
  private String keyword;

  /**
   * 任务描述
   */
  @Schema(name = "description", description = "任务描述")
  private String description;

  /**
   * 已标注数据量
   */
  @Schema(name = "labeledCount", description = "已标注数据量")
  private Long labeledCount = 0L;

  @Schema(name = "unlabeledCount", description = "未标注数据量")
  private Long unlabeledCount = 0L;

  /**
   * 精准率
   */
  @Schema(name = "precise", description = "精准率")
  private String precise;

  /**
   * 召回率
   */
  @Schema(name = "recall", description = "召回率")
  private String recall;

  /**
   * F1值
   */
  @Schema(name = "f1Score", description = "F1值")
  private String f1Score;

  /**
   * 是否删除
   */
  @Schema(name = "deleted", description = "是否删除", type = "boolean")
  private Boolean deleted;

  /**
   * 创建人
   */
  @Schema(name = "createPeople", description = "创建人")
  private String createPeople;

  /**
   * 创建时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(name = "createDatetime", example = "2020-03-31 18:23:00", description = "创建时间")
  private LocalDateTime createDatetime;

  /**
   * 更新人
   */
  @Schema(name = "updatePeople", description = "更新人")
  private String updatePeople;

  /**
   * 更新时间
   */
  @Schema(name = "updateDatetime", description = "更新时间", example = "2020-03-31 18:23:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateDatetime;

}
