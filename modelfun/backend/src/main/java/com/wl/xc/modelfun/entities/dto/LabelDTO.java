package com.wl.xc.modelfun.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/6/2 14:53
 */
@NoArgsConstructor
@Data
public class LabelDTO {

  /**
   * labelDesc
   */
  @JsonProperty("label_desc")
  private String labelDesc;
  /**
   * labelId
   */
  @JsonProperty("label_id")
  private Integer labelId;
}
