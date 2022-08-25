package com.wl.xc.modelfun.entities.req;

import com.wl.xc.modelfun.commons.validation.group.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/20 14:42
 */
@Data
public class DatasetReqDTO {

  @Schema(name = "id", description = "数据库ID")
  @NotNull(message = "记录ID不能为空", groups = {Update.class})
  private Long id;

  @Schema(name = "taskId", description = "任务ID")
  @NotNull(message = "任务ID不能为空", groups = {Update.class})
  private Long taskId;

  @Schema(name = "dataType", description = "数据类型（4：验证集，5：测试集，2：未标注数据集，3：标签集）")
  @NotNull(message = "数据类型不能为空", groups = {Update.class})
  private Integer dataType;

  @Schema(name = "sentence", description = "语料内容，在模糊查询未标注数据集时传入")
  private String sentence;

  @Schema(name = "labelId", description = "标签ID")
  @NotNull(message = "标签ID不能为空", groups = {Update.class})
  private Integer labelId;
}
