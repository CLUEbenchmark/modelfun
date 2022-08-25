package com.wl.xc.modelfun.entities.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl.xc.modelfun.commons.validation.group.Create;
import com.wl.xc.modelfun.commons.validation.group.Delete;
import com.wl.xc.modelfun.commons.validation.group.Retrieve;
import com.wl.xc.modelfun.commons.validation.group.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 任务请求对应实例
 *
 * @version 1.0
 * @date 2022/4/11 16:28
 */
@Data
@Schema(name = "TaskReq", description = "用于查询任务信息的请求参数")
public class TaskReq {

  /**
   * 任务ID
   */
  @Schema(description = "任务ID", example = "1")
  @JsonProperty(value = "id")
  @NotNull(message = "任务ID不能为空", groups = {Delete.class, Update.class})
  private Long id;

  /**
   * 用户id，任务拥有者
   */
  @Schema(description = "用户id，任务拥有者", example = "1")
  @JsonProperty(value = "userId")
  @NotNull(message = "用户ID不能为空", groups = {Retrieve.class, Create.class})
  private Integer userId;

  /**
   * 关键词
   */
  @Schema(description = "关键词", example = "任务", nullable = true)
  @JsonProperty(value = "keyword")
  private String keyword;

  /**
   * 任务名称
   */
  @Schema(description = "任务名称，新建任务时不能为空", example = "任务名称")
  @JsonProperty(value = "name")
  @NotBlank(message = "任务名称不能为空", groups = {Create.class})
  @Length(max = 10, message = "任务名称长度不能超过10个字符", groups = {Create.class})
  private String name;

  /**
   * 任务描述
   */
  @Schema(description = "任务描述，新建任务时不能为空", example = "任务描述")
  @JsonProperty(value = "description")
  @Length(max = 30, message = "任务描述长度不能超过30个字符", groups = {Create.class})
  private String description;

  /**
   * 任务领域
   */
  @Schema(name = "domain", description = "任务领域")
  @NotBlank(message = "任务领域不能为空", groups = {Create.class})
  @JsonProperty("domain")
  private String domain;
  /**
   * 任务类型
   */
  @Schema(name = "taskType", description = "任务类型")
  @NotNull(message = "任务类型不能为空", groups = {Create.class})
  @JsonProperty("taskType")
  private Integer taskType;
  /**
   * 语言类型
   */
  @Schema(name = "languageType", description = "语言类型")
  @NotNull(message = "语言类型不能为空", groups = {Create.class})
  @JsonProperty("languageType")
  private Integer languageType;

  /**
   * 创建时间
   */
  @Schema(hidden = true)
  @JsonProperty(value = "create_datetime")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime createDatetime;

  /**
   * 更新时间
   */
  @Schema(hidden = true)
  @JsonProperty(value = "update_datetime")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private LocalDateTime updateDatetime;

  /**
   * 更新人
   */
  @Schema(hidden = true)
  @JsonProperty(value = "update_people")
  private String updatePeople;

}
