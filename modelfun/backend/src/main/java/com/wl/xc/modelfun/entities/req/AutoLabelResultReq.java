package com.wl.xc.modelfun.entities.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl.xc.modelfun.commons.validation.group.Delete;
import com.wl.xc.modelfun.commons.validation.group.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 自动标注结果请求
 *
 * @version 1.0
 * @date 2022/5/31 15:42
 */
@Data
@Schema(name = "自动标注结果修改请求")
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AutoLabelResultReq extends TaskIdReq {

  @Schema(name = "recordId")
  @NotNull(message = "记录ID不能为空", groups = {Update.class})
  private Long recordId;

  @Schema(name = "labelId")
  @NotNull(message = "标签ID不能为空", groups = {Update.class})
  @JsonProperty("labelId")
  private Integer labelId;

  @Schema(name = "recordIds")
  @NotEmpty(message = "记录ID不能为空", groups = {Delete.class})
  private List<Long> recordIds;
}
