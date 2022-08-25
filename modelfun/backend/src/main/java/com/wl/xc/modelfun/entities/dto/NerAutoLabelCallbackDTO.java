package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl.xc.modelfun.entities.model.NerTestDataModel;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/6/10 11:35
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NerAutoLabelCallbackDTO extends BaseCallbackDTO {

  @JsonProperty("results")
  private ResultDTO results;

  @NoArgsConstructor
  @Data
  public static class ResultDTO {

    /**
     * 标注结果
     */
    @JsonProperty("unlabel_res")
    private List<NerTestDataModel> unlabelRes;
    /**
     * 可信度高的数据
     */
    @JsonProperty("certainty_idx")
    private List<Integer> certaintyIdx;
    /**
     * 添加到训练集的数据
     */
    @JsonProperty("train_idx")
    private List<Integer> trainIdx;
    /**
     * 可信度低的数据
     */
    @JsonProperty("uncertainty_idx")
    private List<Integer> uncertaintyIdx;
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
     * fscore
     */
    @JsonProperty("fscore")
    private Double fscore;
  }
}

