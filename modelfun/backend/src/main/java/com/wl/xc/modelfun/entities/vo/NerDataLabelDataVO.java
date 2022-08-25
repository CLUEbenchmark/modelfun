package com.wl.xc.modelfun.entities.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/5/25 13:44
 */
@NoArgsConstructor
@Schema(name = "NerDataLabelDataVO", description = "Ner语料标签数据")
@Data
public class NerDataLabelDataVO {


  /**
   * 数据库ID，该ID是为了修改时方便查询
   */
  @NotNull(message = "数据ID不能为空")
  @JsonProperty("id")
  @Schema(name = "id", description = "数据库ID，该ID是为了修改时方便查询")
  private Long id;
  /**
   * 语料ID
   */
  @NotNull(message = "语料ID不能为空")
  @JsonProperty("dataId")
  @Schema(name = "dataId", description = "语料ID")
  private Long dataId;
  /**
   * 语料
   */
  @JsonProperty("sentence")
  @Schema(name = "sentence", description = "语料")
  private String sentence;
  /**
   * 数据类型（1：测试集，8：标签集）
   */
  @JsonProperty("dataType")
  @Schema(name = "dataType", description = "数据类型（1：测试集，8：标签集）")
  @NotNull(message = "数据类型不能为空")
  private Integer dataType;
  /**
   * 实体标签列表
   */
  @JsonProperty("labels")
  @Schema(name = "labels", description = "实体标签列表")
  private List<LabelsDTO> labels;

  /**
   * Item
   */
  @NoArgsConstructor
  @Data
  public static class LabelsDTO {

    /**
     * 标签ID
     */
    @JsonProperty("labelId")
    @Schema(name = "labelId", description = "标签ID")
    private Integer labelId;
    /**
     * 标签描述
     */
    @JsonProperty("labelDes")
    @Schema(name = "labelDes", description = "标签描述")
    private String labelDes;
    /**
     * 起始位置
     */
    @JsonProperty("startOffset")
    @Schema(name = "startOffset", description = "起始位置")
    private Integer startOffset;
    /**
     * 结束位置
     */
    @JsonProperty("endOffset")
    @Schema(name = "endOffset", description = "结束位置")
    private Integer endOffset;

    private Long dataId;
  }
}
