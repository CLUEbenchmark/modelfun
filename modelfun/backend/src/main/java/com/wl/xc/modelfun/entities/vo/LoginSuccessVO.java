package com.wl.xc.modelfun.entities.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/3/31 17:02
 */
@Data
@Schema(description = "登录成功返回信息")
public class LoginSuccessVO {

  @Schema(description = "用户ID")
  private Integer id;
  @Schema(description = "用户名")
  private String username;
  @Schema(description = "用户手机号")
  private String userPhone;
  @Schema(description = "登录成功后的token")
  private String token;
}
