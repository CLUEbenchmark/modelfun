package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 数据集详情视图类
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/3/31 18:25
 */
@Data
@Schema(name = "dataSetDetailVO", description = "数据集详情")
public class DataSetDetailVO {

  /**
   * 自增ID
   */
  @Schema(name = "id", description = "数据集ID")
  @JsonProperty(value = "id")
  private Integer id;

  /**
   * 集合名称
   */
  @Schema(name = "setName", description = "集合名称")
  @JsonProperty(value = "setName")
  private String setName;

  /**
   * 集合状态
   */
  @Schema(name = "setStatus", description = "集合状态")
  @JsonProperty(value = "setStatus")
  private Integer setStatus;

  /**
   * 项目ID
   */
  @Schema(name = "projectId", description = "项目ID")
  @JsonProperty(value = "projectId")
  private Integer projectId;

  /**
   * 集合文件地址
   */
  @Schema(name = "setAddress", description = "集合文件地址")
  @JsonProperty(value = "setAddress")
  private String setAddress;

  /**
   * 集合类型（1：训练集，2：测试集，3：未标注数据集）
   */
  @Schema(name = "setType", description = "集合类型（1：训练集，2：测试集，3：未标注数据集）")
  @JsonProperty(value = "setType")
  private Integer setType;

  /**
   * 任务领域
   */
  @Schema(name = "domain", description = "任务领域")
  @JsonProperty(value = "domain")
  private String domain;

  /**
   * 任务类型
   */
  @Schema(name = "type", description = "任务类型")
  @JsonProperty(value = "type")
  private Integer type;

  /**
   * 语言类型
   */
  @Schema(name = "language", description = "语言类型")
  @JsonProperty(value = "language")
  private Integer language;

  /**
   * 数据来源
   */
  @Schema(name = "dataSource", description = "数据来源")
  @JsonProperty(value = "dataSource")
  private String dataSource;

  /**
   * 标签来源
   */
  @Schema(name = "labelSource", description = "标签来源")
  @JsonProperty(value = "labelSource")
  private Integer labelSource;

  /**
   * 集合版本
   */
  @Schema(name = "setVersion", description = "集合版本")
  @JsonProperty(value = "setVersion")
  private String setVersion;

  /**
   * 集合描述
   */
  @Schema(name = "setDesc", description = "集合描述")
  @JsonProperty(value = "setDesc")
  private String setDesc;

  /**
   * 语料数量
   */
  @Schema(name = "sentenceCount", description = "语料数量")
  @JsonProperty(value = "sentenceCount")
  private Integer sentenceCount;

  /**
   * 标签数量
   */
  @Schema(name = "labelCount", description = "标签数量")
  @JsonProperty(value = "labelCount")
  private Integer labelCount;

  /**
   * 创建时间
   */
  @Schema(name = "createDatetime", description = "创建时间")
  @JsonProperty(value = "createDatetime")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDatetime;

  /**
   * 更新时间
   */
  @Schema(name = "updateDatetime", description = "更新时间")
  @JsonProperty(value = "updateDatetime")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateDatetime;

  /**
   * 更新人
   */
  @Schema(hidden = true)
  @JsonProperty(value = "updatePeople")
  private String updatePeople;
}
