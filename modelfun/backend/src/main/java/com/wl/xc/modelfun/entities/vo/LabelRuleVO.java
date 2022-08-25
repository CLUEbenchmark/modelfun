package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/12 9:48
 */
@Data
@Schema(name = "LabelRuleVO", description = "标签规则视图对象")
public class LabelRuleVO {

  /**
   * 规则ID
   */
  @Schema(name = "id", description = "规则ID")
  private Long id;

  /**
   * 任务ID
   */
  @Schema(name = "taskId", description = "任务ID")
  private Long taskId;

  /**
   * 数据集ID
   */
  @Schema(name = "datasetId", description = "数据集ID")
  private Integer datasetId;

  /**
   * 规则类型（1：模式匹配，2：专家知识，3：数据库，4：外部api）
   */
  @Schema(name = "ruleType", description = "规则类型（1：模式匹配，2：专家知识，3：数据库，4：外部api）")
  private Integer ruleType;

  /**
   * 规则名称
   */
  @Schema(name = "ruleName", description = "规则名称")
  private String ruleName;

  /**
   * 标签ID
   */
  @Schema(name = "label", description = "标签ID")
  private Integer label;

  /**
   * 标签名称
   */
  @Schema(name = "labelDes", description = "标签名称")
  private String labelDes;

  /**
   * 准确率
   */
  @Schema(name = "accuracy", description = "准确率")
  private String accuracy;

  /**
   * 冲突
   */
  @Schema(name = "conflict", description = "冲突")
  private String conflict;

  /**
   * 覆盖率
   */
  @Schema(name = "coverage", description = "覆盖率")
  private String coverage;

  /**
   * 覆盖率
   */
  @Schema(name = "unlabeledCoverage", description = "未标注数据集的覆盖率")
  private String unlabeledCoverage;
  /**
   * 重叠率
   */
  @Schema(name = "overlap", description = "重叠率")
  private String overlap;

  /**
   * 规则是否运行完成
   */
  @Schema(name = "completed", description = "规则运行状态。0：运行中，1：运行成功，2：运行失败")
  private Integer completed;

  /**
   * 规则声明描述
   */
  @Schema(name = "metadata", description = "规则声明描述")
  private String metadata;

  /**
   * 创建时间
   */
  @Schema(name = "createDatetime", description = "创建时间", example = "2020-04-12 09:48:00", hidden = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDatetime;

  /**
   * 规则开始创建的时间
   */
  @Schema(name = "createStartTime", description = "规则开始创建的时间", example = "2020-04-12 09:48:00", hidden = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createStartTime;

  /**
   * 规则创建结束的时间
   */
  @Schema(name = "createEndTime", description = "规则创建结束的时间", example = "2020-04-12 09:48:00", hidden = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createEndTime;

  /**
   * 更新时间
   */
  @Schema(name = "updateDatetime", description = "更新时间", example = "2020-04-12 09:48:00", hidden = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateDatetime;

  /**
   * 规则更新开始的时间
   */
  @Schema(name = "updateStartTime", description = "规则更新开始的时间", example = "2020-04-12 09:48:00", hidden = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateStartTime;

  /**
   * 规则更新结束的时间
   */
  @Schema(name = "updateEndTime", description = "规则更新结束的时间", example = "2020-04-12 09:48:00", hidden = true)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateEndTime;

}
