package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/15 16:04
 */
@Data
public class ExpertVO {

  @Schema(description = "专家知识ID", name = "mapKey")
  private Long id;

  @Schema(description = "专家知识文件名称", name = "fileName")
  private String fileName;

  @Schema(description = "专家知识文件地址", name = "address")
  private String address;

}
