package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/5/7 13:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IntegrateCallbackDTO extends BaseCallbackDTO {

  @JsonProperty("results")
  private ResultDTO results;

  @Data
  public static class ResultDTO {

    @JsonProperty("label_model_path")
    private String labelModelPath;

    @JsonProperty("mapping_model_path")
    private String mappingModelPath;
  }

}
