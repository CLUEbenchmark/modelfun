package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 18:12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class DataSetParse {

  private Long id;

  private Integer label;

  @JsonAlias({"text"})
  @JsonProperty("sentence")
  private String sentence;

  @JsonProperty("label_des")
  private String labelDes;

}
