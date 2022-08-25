package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/20 14:53
 */
@Data
@Schema(name = "LabelFunctionTestReq", description = "函数代码规则测试请求")
public class LabelFunctionTestReq {

  @Schema(description = "方法名")
  @NotBlank(message = "方法名不能为空")
  private String functionName;

  @Schema(description = "方法体")
  @NotBlank(message = "方法体不能为空")
  private String functionBody;
  /**
   * 用户输入用于测试lf的样例
   */
  @Schema(description = "样例语句")
  @NotBlank(message = "样例语句不能为空")
  private String example;
}
