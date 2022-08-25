package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/6/9 17:10
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NerTrainCallbackDTO extends BaseCallbackDTO {

  @JsonProperty("results")
  private ResultDTO results;

  @NoArgsConstructor
  @Data
  public static class ResultDTO {

    /**
     * unlabelRes
     */
    @JsonProperty("test_res")
    private List<UnlabelResDTO> testRes;
    @JsonProperty("model_path")
    private String modelPath;
    /**
     * accuracy
     */
    @JsonProperty("accuracy")
    private Double accuracy;
    /**
     * precision
     */
    @JsonProperty("precision")
    private Double precision;
    /**
     * recall
     */
    @JsonProperty("recall")
    private Double recall;
    /**
     * fscore
     */
    @JsonProperty("fscore")
    private Double fscore;
    /**
     * report
     */
    @JsonProperty("report")
    private Map<String, Arg> report;
  }

  @NoArgsConstructor
  @Data
  public static class Arg {

    /**
     * precision
     */
    @JsonProperty("precision")
    private Double precision;
    /**
     * recall
     */
    @JsonProperty("recall")
    private Double recall;
    /**
     * f1score
     */
    @JsonProperty("f1-score")
    private Double f1score;
    /**
     * support
     */
    @JsonProperty("support")
    private Integer support;
  }

  /**
   * UnlabelResDTO
   */
  @NoArgsConstructor
  @Data
  public static class UnlabelResDTO {

    private Integer id;

    /**
     * text
     */
    @JsonProperty("text")
    private String text;
    /**
     * entities
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
       * label
       */
      @JsonProperty("label")
      private String label;
      /**
       * startOffset
       */
      @JsonProperty("start_offset")
      private Integer startOffset;
      /**
       * endOffset
       */
      @JsonProperty("end_offset")
      private Integer endOffset;

      public boolean simpleEqual(EntitiesDTO other) {
        return this.label.equals(other.label)
            && this.startOffset.equals(other.startOffset)
            && this.endOffset.equals(other.endOffset);
      }
    }
  }
}
