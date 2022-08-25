package com.wl.xc.modelfun.config.properties;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/5/13 11:07
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.websocket")
public class WebsocketProperties {


  /**
   * 心跳间隔时间，单位为秒，默认为30秒
   */
  private Duration heartBeatTime = Duration.ofSeconds(30);
  /**
   * 心跳超时次数，连续多次没有发送心跳包，则认为连接已断开，默认为3次
   */
  private int maxIdleCount = 3;
}
