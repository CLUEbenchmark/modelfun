package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * 模型训练结果
 *
 * @version 1.0
 * @date 2022/4/12 11:31
 */
@Data
@Schema(name = "TrainRecordVO", description = "模型训练结果")
public class TrainRecordVO {

  /**
   * 模型训练记录ID
   */
  @Schema(name = "trainRecordId", description = "模型训练记录ID")
  private Long trainRecordId;

  /**
   * 数据版本
   */
  @Schema(name = "dataVersion", description = "数据版本")
  private String dataVersion;

  /**
   * 下载数据文件地址
   */
  @Schema(name = "dataFileAddress", description = "下载数据文件地址")
  private String dataFileAddress;

  /**
   * 模型文件地址
   */
  @Schema(name = "modelFileAddress", description = "模型文件地址")
  private String modelFileAddress;

  /**
   * 训练结果
   */
  @Schema(name = "trainResult", description = "训练结果")
  private List<TrainResultVO> trainResult;
}
