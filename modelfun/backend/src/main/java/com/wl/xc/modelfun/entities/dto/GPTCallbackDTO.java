package com.wl.xc.modelfun.entities.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/5/18 10:10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GPTCallbackDTO extends BaseCallbackDTO {

  private String labels;

}
