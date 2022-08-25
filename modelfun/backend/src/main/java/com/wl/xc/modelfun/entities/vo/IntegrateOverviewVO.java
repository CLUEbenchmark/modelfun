package com.wl.xc.modelfun.entities.vo;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.DEFAULT_TIME_ZONE;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/5 10:45
 */
@Data
public class IntegrateOverviewVO {

  /**
   * 训练集标签类别数
   */
  @Schema(description = "训练集标签类别数")
  private Integer trainLabelCount;
  /**
   * 训练集语料标注量
   */
  @Schema(description = "训练集语料标注量")
  private Long trainSentenceCount;
  /**
   * 测试集的准确率
   */
  @Schema(description = "测试集的准确率")
  private String testAccuracy;
  /**
   * 测试集的标注结果召回率
   */
  @Schema(description = "测试集的标注结果召回率")
  private String testRecall;
  /**
   * 测试集的F1值
   */
  @Schema(description = "测试集的F1值")
  private String testF1Score;
  /**
   * 未标注数据集的覆盖率
   */
  @Schema(description = "未标注数据集的覆盖率")
  private String unlabelCoverage;
  /**
   * 耗时
   */
  @Schema(description = "耗时")
  private String timeCost;

  /**
   * 最近更新时间
   */
  @Schema(description = "最近更新时间", example = "2020-04-14 13:59:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = DEFAULT_TIME_ZONE)
  private LocalDateTime lastUpdateTime;

}
