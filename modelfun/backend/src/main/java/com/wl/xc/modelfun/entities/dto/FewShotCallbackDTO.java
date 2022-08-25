package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/6/21 16:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FewShotCallbackDTO extends BaseCallbackDTO {

  @JsonProperty("results")
  private ResultDTO results;

  @Data
  public static class ResultDTO {

    @JsonProperty("unlabeled_predictions")
    private List<Integer> unlabeledPredictions;

    @JsonProperty("test_predictions")
    private List<Integer> testPredictions;

    @JsonProperty("val_predictions")
    private List<Integer> valPredictions;
  }
}
