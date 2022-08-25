package com.wl.xc.modelfun.entities.dto;

import java.util.List;
import lombok.Data;

/**
 * token的payload
 *
 * @version 1.0
 * @date 2022/4/12 18:36
 */
@Data
public class PayloadDTO {

  /**
   * 主题
   */
  private String sub;

  /**
   * 签发时间
   */
  private Long iat;

  /**
   * 过期时间
   */
  private Long exp;

  /**
   * JWT ID
   */
  private String jti;

  /**
   * 用户ID
   */
  private Integer userId;

  /**
   * 用户名
   */
  private String username;

  private String userPhone;

  /**
   * 用户权限
   */
  private List<String> authorities;
}
