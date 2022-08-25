package com.wl.xc.modelfun.config;

import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.config.properties.EventThreadPoolProperties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * 用于spring事件配置
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/6 10:23
 */
@Configuration
@EnableConfigurationProperties(EventThreadPoolProperties.class)
public class SpringEventConfig {

  private final EventThreadPoolProperties properties;

  public SpringEventConfig(EventThreadPoolProperties eventThreadPoolProperties) {
    this.properties = eventThreadPoolProperties;
  }

  @Bean(name = AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
  public ApplicationEventMulticaster applicationEventMulticaster(BeanFactory beanFactory) {
    SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster(
        beanFactory);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(properties.getCorePoolSize(),
        properties.getMaximumPoolSize(), properties.getKeepAliveTime(), TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(properties.getQueueCapacity()), new WorkThreadFactory("event-multicaster"));
    eventMulticaster.setTaskExecutor(executor);
    return eventMulticaster;
  }
}
