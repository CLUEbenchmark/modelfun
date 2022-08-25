package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/14 16:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "LabelPageReq", description = "用于标签分页查询")
public class LabelPageReq extends TaskIdPageReq {

  @Schema(description = "标签描述")
  private String labelDesc;
}
