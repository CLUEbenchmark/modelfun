package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/7/27 17:28
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.db")
public class CustomerDatasourceProperties {

  private String url = null;

  private String username = null;

  private String password = null;

  private String database = null;
}
