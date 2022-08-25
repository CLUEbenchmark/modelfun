package com.wl.xc.modelfun.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * 对于json的配置，使得全项目使用统一的配置。 这里默认使用jackson作为json序列化工具，并且项目内使用同一个ObjectMapper
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/3/8 10:10
 */
@Configuration
public class JsonConfig {

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * 使用spring提供的jackson配置类代替自己生成ObjectMapper。
   * <p>
   * {@link Jackson2ObjectMapperBuilder} 定义了一些常规配置，并且会自动引入jackson的Module
   *
   * @return 配置类
   */
  @Bean
  Jackson2ObjectMapperBuilderCustomizer customizer() {
    return builder -> {
      builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
      builder.serializationInclusion(JsonInclude.Include.NON_NULL);
      builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
      builder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    };
  }

}
