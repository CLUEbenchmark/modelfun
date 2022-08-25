package com.wl.xc.modelfun.config.properties;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @version 1.0
 * @date 2022/4/18 13:51
 */
@Data
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.security")
public class SecurityProperties {

  private List<String> ignoreUrls;

}
