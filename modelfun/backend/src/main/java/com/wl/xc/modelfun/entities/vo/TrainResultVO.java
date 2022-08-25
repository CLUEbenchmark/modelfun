package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/12 13:20
 */
@Data
@Schema(name = "TrainResultVO", description = "训练结果")
public class TrainResultVO {

  /**
   * 模型训练结果记录ID
   */
  @Schema(name = "id", description = "模型训练结果记录ID")
  private Long id;

  /**
   * 数据版本
   */
  @Schema(name = "dataVersion", description = "数据版本")
  private String dataVersion;

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
   * 训练集数量
   */
  @Schema(name = "trainCount", description = "训练集数量")
  private Integer trainCount;

  /**
   * 训练记录ID
   */
  @Schema(name = "trainRecordId", description = "训练记录ID")
  private Long trainRecordId;

  /**
   * 标注规则数量
   */
  @Schema(name = "ruleCount", description = "标注规则数量")
  private Integer ruleCount;

  /**
   * 标签类别数量
   */
  @Schema(name = "labelTypeCount", description = "标签类别数量")
  private Integer labelTypeCount;

  /**
   * 覆盖率
   */
  @Schema(name = "coverage", description = "覆盖率")
  private String coverage;

  /**
   * 准确率
   */
  @Schema(name = "accuracy", description = "准确率")
  private String accuracy;

  /**
   * 精准率
   */
  @Schema(name = "trainPrecision", description = "精准率")
  private String trainPrecision;

  /**
   * 召回率
   */
  @Schema(name = "recall", description = "召回率")
  private String recall;

  /**
   * f1 score
   */
  @Schema(name = "f1Score", description = "f1 score")
  private String f1Score;

  /**
   * 模型类型
   */
  @Schema(name = "modelType", description = "模型类型")
  private String modelType;

  /**
   * 模型文件地址
   */
  @Schema(name = "modelFileAddress", description = "模型文件地址", hidden = true)
  private String modelFileAddress;

  /**
   * 训练文件地址
   */
  @Schema(name = "trainFileAddress", description = "训练文件地址", hidden = true)
  private String trainFileAddress;

  /**
   * 创建时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(name = "createDatetime", example = "2020-03-31 18:23:00", description = "创建时间")
  private LocalDateTime createDatetime;

  @Schema(name = "trainStatus", description = "训练结果，0：训练中，1：训练成功，2：训练失败-内部错误，3：训练失败-网络错误")
  private Integer trainStatus;
}
