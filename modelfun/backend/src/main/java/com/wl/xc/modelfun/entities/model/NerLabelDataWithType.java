package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/5/24 14:29
 */
@NoArgsConstructor
@Data
@JsonInclude(Include.NON_NULL)
public class NerLabelDataWithType {

  /**
   * 数据ID
   */
  @JsonProperty("id")
  private Long id;
  /**
   * 语料内容
   */
  @JsonProperty("text")
  private String text;
  /**
   * 关系，目前未知
   */
  @JsonProperty("relations")
  private List<Object> relations;
  /**
   * 数据类型。训练集、高置信数据、待审核数据
   */
  private String type;
  /**
   * 实体列表
   */
  @JsonProperty("entities")
  private List<EntitiesDTO> entities;

  /**
   * EntitiesDTO
   */
  @NoArgsConstructor
  @Data
  public static class EntitiesDTO {

    /**
     * 实体ID
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
