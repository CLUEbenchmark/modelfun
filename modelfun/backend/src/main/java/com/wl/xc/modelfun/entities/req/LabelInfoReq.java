package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签信息请求
 *
 * @version 1.0
 * @date 2022/5/16 17:25
 */
@Data
@Schema(name = "LabelInfoReq", description = "用于更新标签集信息")
public class LabelInfoReq {

  @Schema(description = "标签记录ID")
  private Long id;
  @Schema(description = "任务ID")
  private Long taskId;
  @Schema(description = "标签ID")
  private Long labelId;
  @Schema(description = "标签描述")
  private String labelDesc;
  @Schema(description = "标签说明")
  private String description;
  @Schema(description = "示例说明")
  private String example;

}
