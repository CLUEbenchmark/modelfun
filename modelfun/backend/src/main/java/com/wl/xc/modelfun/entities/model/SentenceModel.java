package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/25 16:47
 */
@Data
public class SentenceModel {

  @JsonProperty("sentence")
  private String sentence;
}
