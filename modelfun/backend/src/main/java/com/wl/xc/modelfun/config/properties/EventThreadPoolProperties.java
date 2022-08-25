package com.wl.xc.modelfun.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * spring事件广播线程池配置
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/6 10:44
 */
@ConfigurationProperties(prefix = "com.wl.xc.modelfun.event.thread-pool")
@Data
public class EventThreadPoolProperties {

  /**
   * 核心线程数。默认值：20
   */
  private int corePoolSize = 20;
  /**
   * 最大线程数，默认值：200
   */
  private int maximumPoolSize = 200;
  /**
   * 非核心线程空闲时间，单位毫秒。默认60000毫秒
   */
  private long keepAliveTime = 60 * 1000;
  /**
   * 线程池缓冲队列大小，缓冲队列使用的是LinkedBlockingQueue，默认值5000
   */
  private int queueCapacity = 5000;
}
