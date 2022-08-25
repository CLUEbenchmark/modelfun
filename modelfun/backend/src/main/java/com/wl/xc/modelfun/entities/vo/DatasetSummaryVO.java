package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 数据集概要信息
 *
 * @version 1.0
 * @date 2022/4/13 18:07
 */
@Data
@Schema(name = "DatasetSummaryVO", description = "数据集概要信息")
public class DatasetSummaryVO {

  /**
   * 上传时间
   */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  @Schema(name = "uploadDateTime", example = "2020-03-31 18:23:00", description = "上传时间")
  private LocalDateTime uploadDateTime;
  /**
   * 标签类别
   */
  @Schema(name = "labelCount", description = "标签类别")
  private Long labelCount;
  /**
   * 测试集数量
   */
  @Schema(name = "testDataCount", description = "测试集数量")
  private Long testDataCount;
  /**
   * 训练集数量
   */
  @Schema(name = "trainDataCount", description = "训练集数量")
  private Long trainDataCount;
  /**
   * 测试集类别
   */
  @Schema(name = "testDataTypeCount", description = "测试集类别")
  private Long testDataTypeCount;
  /**
   * 未标注数量
   */
  @Schema(name = "unlabelCount", description = "未标注数量")
  private Long unlabelCount;
  /**
   * 是否存在解析任务
   */
  @Schema(name = "exitParseTask", description = "是否存在解析任务")
  private Boolean exitParseTask;
  /**
   * 是否存在解析任务
   */
  @Schema(name = "exitClickTask", description = "是否存在一键标注任务")
  private Boolean exitClickTask;
  /**
   * 解析任务ID
   */
  @Schema(name = "requestId", description = "解析任务ID")
  private String requestId;

}
