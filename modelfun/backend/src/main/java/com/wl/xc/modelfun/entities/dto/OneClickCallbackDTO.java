package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 一键标注回调对象
 *
 * @version 1.0
 * @date 2022/5/26 14:25
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OneClickCallbackDTO extends BaseCallbackDTO {


  /**
   * 结果
   */
  @JsonProperty("results")
  private ResultsDTO results;

  /**
   * ResultsDTO
   */
  @NoArgsConstructor
  @Data
  public static class ResultsDTO {

    /**
     * 实体标签
     */
    @JsonProperty("label")
    private List<LabelDTO> label;
    /**
     * 准确率
     */
    @JsonProperty("accuracy")
    private Double accuracy;
    /**
     * 精准率
     */
    @JsonProperty("precision")
    private Double precision;
    /**
     * 召回率
     */
    @JsonProperty("recall")
    private Double recall;
    /**
     * f1
     */
    @JsonProperty("fscore")
    private Double fscore;

  }

  /**
   * LabelDTO
   */
  @NoArgsConstructor
  @Data
  public static class LabelDTO {

    /**
     * 实体联系，暂时无用
     */
    @JsonProperty("relations")
    private List<?> relations;
    /**
     * 实体列表
     */
    @JsonProperty("entities")
    private List<EntitiesDTO> entities;

  }

  /**
   * EntitiesDTO
   */
  @NoArgsConstructor
  @Data
  public static class EntitiesDTO {

    /**
     * ID
     */
    @JsonProperty("id")
    private Integer id;
    /**
     * 开始位置
     */
    @JsonProperty("start_offset")
    private Integer startOffset;
    /**
     * 结束位置
     */
    @JsonProperty("end_offset")
    private Integer endOffset;
    /**
     * 标签名称
     */
    @JsonProperty("label")
    private String label;
  }
}
