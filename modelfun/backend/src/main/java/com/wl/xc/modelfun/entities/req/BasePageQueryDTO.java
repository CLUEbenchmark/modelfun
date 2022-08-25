package com.wl.xc.modelfun.entities.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 基础分页查询DTO
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/9/8 15:04
 */
@Schema(name = "基础分页查询请求对象")
@Data
public class BasePageQueryDTO {

  /**
   * 当前页码，从1开始，默认1
   */
  @Schema(description = "当前页码，从1开始，默认1", name = "pageNum", example = "1")
  @JsonProperty("current")
  private Long curPage = 1L;
  /**
   * 每页大小，默认15
   */
  @Schema(description = "每页大小，默认15", name = "pageSize", example = "15")
  @JsonProperty("pageSize")
  private Long pageSize = 15L;

}
