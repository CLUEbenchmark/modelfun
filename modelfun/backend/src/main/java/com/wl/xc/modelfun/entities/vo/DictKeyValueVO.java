package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/4/12 15:14
 */
@Data
@Schema(description = "键值对", name = "DictKeyValueVO")
@AllArgsConstructor
@NoArgsConstructor
public class DictKeyValueVO {

  @Schema(description = "键", name = "mapKey")
  private String mapKey;

  @Schema(description = "值", name = "mapValue")
  private String mapValue;

  @Schema(description = "排序", name = "mapSort")
  private Integer mapSort;

}
