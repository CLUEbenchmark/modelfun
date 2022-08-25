package com.wl.xc.modelfun.entities.req;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.DEFAULT_TIME_ZONE;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.validation.EnumValidator;
import com.wl.xc.modelfun.commons.validation.group.Create;
import com.wl.xc.modelfun.commons.validation.group.Delete;
import com.wl.xc.modelfun.commons.validation.group.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 操作标注规则的请求，操作可以是创建，更新，删除
 *
 * @version 1.0
 * @date 2022/4/14 13:59
 */
@Data
@Schema(description = "操作标注规则的请求")
public class RuleOpReq {

  /**
   * 任务ID
   */
  @NotNull(groups = {Create.class}, message = "任务ID不能为空")
  @Schema(description = "任务ID", example = "1")
  private Long taskId;

  /**
   * 规则ID
   */
  @NotNull(groups = {Delete.class, Update.class}, message = "规则ID")
  @Schema(description = "规则ID", example = "1")
  private Long ruleId;

  /**
   * 标注规则名称
   */
  @NotBlank(message = "标注规则名称不能为空", groups = {Create.class})
  @Schema(description = "标注规则名称", required = true, example = "标注规则名称")
  private String ruleName;
  /**
   * 标注规则类型
   */
  @EnumValidator(value = RuleType.class, method = "getType", message = "标注规则类型不存在", groups = {Create.class})
  @Schema(description = "标注规则类型", required = true, example = "1", allowableValues = {"1", "2", "3", "4", "5"})
  private Integer ruleType;

  /**
   * 规则内容
   */
  @NotBlank(message = "规则内容不能为空", groups = {Create.class})
  @Schema(description = "规则内容", required = false, example = "规则内容")
  private String metadata;

  /**
   * 标签ID
   */
  @Schema(description = "标签ID，只有模式匹配才需要", example = "1")
  private Integer label;

  /**
   * 标签内容
   */
  @Schema(description = "标签内容，只有模式匹配才需要", example = "标签内容")
  private String labelDes;

  /**
   * 规则创建开始时间
   */
  @NotNull(message = "规则创建开始时间不能为空", groups = {Create.class})
  @Schema(description = "规则创建开始时间", required = false, example = "2020-04-14 13:59:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = DEFAULT_TIME_ZONE)
  private LocalDateTime createStartTime;
  /**
   * 规则创建结束时间
   */
  @NotNull(message = "规则创建结束时间不能为空", groups = {Create.class})
  @Schema(description = "规则创建结束时间", required = false, example = "2020-04-14 13:59:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = DEFAULT_TIME_ZONE)
  private LocalDateTime createEndTime;
  /**
   * 规则更新开始时间
   */
  @NotNull(message = "规则更新开始时间不能为空", groups = {Update.class})
  @Schema(description = "规则更新开始时间", required = false, example = "2020-04-14 13:59:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = DEFAULT_TIME_ZONE)
  private LocalDateTime updateStartTime;
  /**
   * 规则更新结束时间
   */
  @NotNull(message = "规则更新结束时间不能为空", groups = {Update.class})
  @Schema(description = "规则更新结束时间", required = false, example = "2020-04-14 13:59:00")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = DEFAULT_TIME_ZONE)
  private LocalDateTime updateEndTime;
}
