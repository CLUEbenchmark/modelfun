package com.wl.xc.modelfun.config.web;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @version 1.0
 * @date 2022/4/13 13:59
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    if (converters.isEmpty()) {
      return;
    }
    // 不要GsonHttpMessageConverter
    converters.removeIf(converter -> converter instanceof GsonHttpMessageConverter);
  }
}
