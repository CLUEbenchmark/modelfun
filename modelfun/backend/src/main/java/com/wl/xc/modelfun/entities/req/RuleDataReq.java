package com.wl.xc.modelfun.entities.req;

import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.validation.EnumValidator;
import com.wl.xc.modelfun.commons.validation.group.Base;
import com.wl.xc.modelfun.commons.validation.group.Retrieve;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/7/5 11:24
 */
@Data
@Schema(description = "标注规则和数据集关联查询的请求实例")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class RuleDataReq extends BasePageQueryDTO {

  /**
   * 任务ID
   */
  @NotNull(groups = {Base.class}, message = "任务ID不能为空")
  @Schema(description = "任务ID", example = "1")
  private Long taskId;

  /**
   * 规则ID
   */
  @NotNull(groups = {Retrieve.class}, message = "规则ID")
  @Schema(description = "规则ID", example = "1")
  private Long ruleId;

  /**
   * 标注规则类型
   */
  @EnumValidator(value = RuleType.class, method = "getType", message = "标注规则类型不存在", groups = {Retrieve.class})
  @Schema(description = "标注规则类型", required = true, example = "1", allowableValues = {"1", "2", "3", "4", "5"})
  private Integer ruleType;

  /**
   * 规则内容
   */
  @Schema(description = "规则内容，只有当模式匹配的时候才需要", required = false, example = "规则内容")
  private String metadata;

  /**
   * 标签ID
   */
  @Schema(description = "标签ID，只有模式匹配才需要", example = "1")
  private Integer label;
}
