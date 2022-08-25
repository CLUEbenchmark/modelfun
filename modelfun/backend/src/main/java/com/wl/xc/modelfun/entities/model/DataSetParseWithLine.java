package com.wl.xc.modelfun.entities.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 18:12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataSetParseWithLine extends DataSetParse {

  private Long line;
}
