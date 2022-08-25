package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 任务规则概览表
 *
 * @version 1.0
 * @date 2022/4/15 12:18
 */
@Data
@Schema(description = "标注概览", example = "400", name = "RuleOverviewVO")
public class RuleOverviewVO {

  /**
   * 数据集标签类别数量
   */
  @Schema(description = "数据集标签类别数量", example = "400")
  private Long labelCount;

  /**
   * 准确率
   */
  @Schema(description = "准确率", example = "40")
  private String accuracy;

  /**
   * 冲突率
   */
  @Schema(description = "冲突率", example = "0.4")
  private String conflict;

  /**
   * 覆盖率
   */
  @Schema(description = "覆盖率", example = "40")
  private String coverage;

  /**
   * 测试集覆盖率
   */
  @Schema(description = "测试集覆盖率", example = "40")
  private String testDataCoverage;

  /**
   * 是否存在解析任务
   */
  @Schema(name = "exitParseTask", description = "是否存在专家解析任务")
  private Boolean exitParseTask;
  /**
   * 解析任务ID
   */
  @Schema(name = "requestId", description = "解析任务ID")
  private String requestId;


}