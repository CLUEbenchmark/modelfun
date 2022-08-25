package com.wl.xc.modelfun.entities.req;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/14 9:55
 */
@Data
@Schema(description = "登录请求")
public class LoginReq {

  @NotBlank(message = "用户名不能为空")
  @Schema(description = "用户名")
  private String username;

  @NotBlank(message = "手机号码不能为空")
  @Schema(description = "手机号码")
  private String userPhone;

  @NotBlank(message = "密码不能为空")
  @Schema(description = "密码")
  private String password;
}
