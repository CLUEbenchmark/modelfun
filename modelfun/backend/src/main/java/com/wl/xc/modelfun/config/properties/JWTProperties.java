package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/4/12 18:59
 */
@Component
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.jwt.secret")
@Data
public class JWTProperties {

  /**
   * 密钥,长度必须大于32位
   */
  private String secret;

}
