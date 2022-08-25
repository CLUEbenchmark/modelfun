package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wl.xc.modelfun.commons.enums.BuiltinModelType;
import com.wl.xc.modelfun.commons.validation.EnumValidator;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/5/18 13:46
 */
@NoArgsConstructor
@Data
@Schema(description = "内置模型")
public class BuiltinModelRule {

  @NotNull
  @Schema(description = "任务ID")
  private Long taskId;

  /**
   * 测试接口使用的字段
   */
  @NotBlank(message = "测试文本不能为空")
  @JsonProperty("texts")
  @Schema(description = "测试文本")
  private String texts;
  /**
   * 示例
   */
  @NotEmpty(message = "标签说明示例")
  @JsonProperty("example")
  @Schema(description = "标签说明示例")
  @Valid
  private List<ExampleDTO> example;
  /**
   * 标签集合
   */
  @NotEmpty(message = "标签类型")
  @JsonProperty("labels")
  @Schema(description = "标签类型")
  private List<Integer> labels;
  /**
   * 目前支持三种类型：1：gpt3, 2: sim, 3: roberta
   */
  @EnumValidator(value = BuiltinModelType.class, method = "getType", message = "内置模型类型不正确")
  @JsonProperty("modelName")
  @Schema(description = "内置模型类型")
  private Integer modelName;

  /**
   * gpt模型所需要的key
   */
  @JsonProperty("appkey")
  @Schema(description = "gpt模型所需要的key")
  private String appKey;

  /**
   * Item
   */
  @NoArgsConstructor
  @Data
  public static class ExampleDTO {

    /**
     * 语料内容
     */
    @JsonProperty("sentence")
    @Schema(description = "语料内容")
    @NotBlank(message = "语料内容不能为空")
    private String sentence;
    /**
     * 标签ID
     */
    @JsonProperty("labelId")
    @JsonAlias("label")
    @Schema(description = "标签ID")
    @NotNull(message = "标签ID不能为空")
    private Integer labelId;
    /**
     * 标签描述
     */
    @JsonProperty("labelDes")
    @JsonAlias("label_des")
    @Schema(description = "标签描述")
    @NotBlank(message = "标签描述不能为空")
    private String labelDes;
  }
}
