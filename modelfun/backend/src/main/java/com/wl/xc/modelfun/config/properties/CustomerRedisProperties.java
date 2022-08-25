package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/7/29 17:46
 */
@Data
@Component
@ConfigurationProperties("com.wl.xc.modelfun.redis")
public class CustomerRedisProperties {

  private String host = null;

  private Integer port = null;

  private String password = null;

  private Integer database = null;
}
