package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/5/20 10:35
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.common")
public class CommonProperties {

  /**
   * 当前激活的环境，默认为空
   */
  private String profile = "";
  /**
   * 是否开启debug模式，默认false
   */
  private Boolean debug = false;
  /**
   * 模式匹配下未标注数据的实时计算数量
   */
  private int unlabelSize = 1000;
}
