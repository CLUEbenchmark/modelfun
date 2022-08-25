package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class NerTestDataModel {

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
    @Schema(name = "id", description = "id")
    private Integer id;
    /**
     * 开始位置
     */
    @JsonProperty("start_offset")
    @Schema(name = "startOffset", description = "开始位置")
    private Integer startOffset;
    /**
     * 结束位置
     */
    @JsonProperty("end_offset")
    @Schema(name = "endOffset", description = "结束位置")
    private Integer endOffset;
    /**
     * 标签名称
     */
    @JsonProperty("label")
    @Schema(name = "label", description = "标签名称")
    private String label;
  }
}
