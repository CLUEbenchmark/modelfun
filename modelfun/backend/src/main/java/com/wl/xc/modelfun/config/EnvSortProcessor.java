package com.wl.xc.modelfun.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * 对环境变量进行重排序，把spring.config.additional-location指定文件的配置提到最前，覆盖其他配置
 *
 * @version 1.0
 * @date 2022/7/29 15:38
 */
public class EnvSortProcessor implements EnvironmentPostProcessor {

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    MutablePropertySources propertySources = environment.getPropertySources();
    PropertySource<?> propertySourceCa = null;
    for (PropertySource<?> propertySource : propertySources) {
      if (propertySource instanceof OriginTrackedMapPropertySource) {
        propertySourceCa = propertySource;
        propertySources.remove(propertySource.getName());
        break;
      }
    }
    if (propertySourceCa != null) {
      propertySources.addFirst(propertySourceCa);
    }
  }
}
