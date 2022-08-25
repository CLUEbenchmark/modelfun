package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 数据集详情请求
 *
 * @version 1.0
 * @date 2022/4/11 17:42
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
@Schema(name = "DatasetDetailReq", description = "用于分页查询数据集内容的请求参数")
public class DatasetDetailReq extends BasePageQueryDTO {

  @Schema(name = "taskId", description = "任务ID")
  @NotNull(message = "任务ID不能为空")
  private Long taskId;

  @Schema(name = "dataType", description = "数据类型（4：验证集，5：测试集，2：未标注数据集，3：标签集）")
  private Integer dataType;

  @Schema(name = "sentence", description = "语料内容，在模糊查询未标注数据集时传入")
  private String sentence;

  @Schema(name = "labelId", description = "标签ID")
  private Integer labelId;

  @Schema(name = "description", description = "标签说明")
  private String description;

  private boolean desc = true;

}
