package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 集成结果视图类
 *
 * @version 1.0
 * @date 2022/4/12 10:43
 */
@Data
@Schema(name = "IntegrationResultVO", description = "规则集成结果")
public class IntegrationResultVO {

  /**
   * 集成结果记录ID
   */
  @Schema(name = "id", description = "集成结果记录ID")
  private Integer id;

  /**
   * 任务ID
   */
  @Schema(name = "taskId", description = "任务ID")
  private Long taskId;

  /**
   * 数据集ID
   */
  @Schema(name = "datasetId", description = "数据集ID", hidden = true)
  private Integer datasetId;

  /**
   * 数据集成记录ID
   */
  @Schema(name = "integrationId", description = "数据集成记录ID", hidden = true)
  private Long integrationId;

  /**
   * 规则ID
   */
  @Schema(name = "ruleId", description = "规则ID")
  private Long ruleId;

  /**
   * 规则名称
   */
  @Schema(name = "ruleName", description = "规则名称")
  private String ruleName;

  /**
   * 标签ID
   */
  @Schema(name = "labelId", description = "标签ID")
  private Integer labelId;

  /**
   * 标签描述
   */
  @Schema(name = "labelDes", description = "标签描述")
  private String labelDes;

  /**
   * 准确率
   */
  @Schema(name = "accuracy", description = "准确率")
  private String accuracy;

  /**
   * 覆盖率
   */
  @Schema(name = "coverage", description = "覆盖率")
  private String coverage;

  /**
   * 重复率
   */
  @Schema(name = "repeat", description = "重复率")
  private String repeat;

  /**
   * 冲突率
   */
  @Schema(name = "conflict", description = "冲突率")
  private String conflict;

  /**
   * 创建时间
   */
  @Schema(name = "createDatetime", description = "创建时间", example = "2020-04-12 09:48:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDatetime;

}
