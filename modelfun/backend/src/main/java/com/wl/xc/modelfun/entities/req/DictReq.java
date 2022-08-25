package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/12 15:25
 */
@Data
@Schema(name = "DictReq", description = "字典查询请求")
public class DictReq {

  @Schema(name = "mapGroup", description = "字典的分组")
  private String mapGroup;

  @Schema(name = "mapKey", description = "字典的key")
  private String mapKey;

}
