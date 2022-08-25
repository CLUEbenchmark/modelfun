package com.wl.xc.modelfun.entities.vo;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.DEFAULT_TIME_ZONE;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/11 17:30
 */
@Data
@Schema(name = "DatasetInfoVO", description = "数据集内容")
public class DatasetInfoVO {

  /**
   * 数据库ID
   */
  @Schema(name = "数据库ID", description = "id")
  private Long id;

  /**
   * 数据ID
   */
  @Schema(name = "数据", description = "dataId")
  private Long dataId;

  /**
   * 数据集类型（1：测试集，2：未标注数据集，3：标签集）
   */
  @Schema(name = "dataType", description = "数据类型（1：测试集，2：未标注数据集，3：标签集）")
  private Integer dataType;

  /**
   * 标签ID
   */
  @Schema(name = "label", description = "标签ID")
  private String label;

  /**
   * 语料内容
   */
  @Schema(name = "sentence", description = "语料内容")
  private String sentence;

  /**
   * 标签内容
   */
  @Schema(name = "labelDes", description = "标签内容")
  private String labelDes;

  @Schema(name = "description", example = "标签描述", description = "标签描述")
  private String description;

  @Schema(name = "example", example = "标签示例", description = "标签示例")
  private String example;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = DEFAULT_TIME_ZONE)
  @Schema(name = "updateDatetime", example = "2020-03-31 18:23:00", description = "更新时间")
  private LocalDateTime updateDatetime;
  /**
   * 是否为匹配到的数据
   */
  private Integer flag;
}
