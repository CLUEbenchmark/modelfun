package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/9 14:19
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class NerUnLabel {

  @JsonProperty("text")
  private String text;

}
