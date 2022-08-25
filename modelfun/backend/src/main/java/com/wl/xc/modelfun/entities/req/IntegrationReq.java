package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/4/12 10:52
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "IntegrationReq", description = "规则集成分页查询请求对象")
public class IntegrationReq extends BasePageQueryDTO {

  @NotNull(message = "任务ID不能为空")
  @Schema(name = "taskId", description = "任务ID")
  private Long taskId;

  @Schema(name = "sentence", description = "语料", required = false)
  private String sentence;

  @Schema(name = "labelId", description = "标签ID", required = false)
  private Integer labelId;

  @NotNull(message = "数据类型不能为空")
  @Schema(name = "dataType", description = "数据类型，1：高置信数据；2：待审核数据", required = true)
  private Integer dataType;

}
