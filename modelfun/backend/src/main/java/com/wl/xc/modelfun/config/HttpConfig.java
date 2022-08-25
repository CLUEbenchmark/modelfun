package com.wl.xc.modelfun.config;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.DEFAULT_KEEP_ALIVE_TIME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.MAX_RETRY;

import com.wl.xc.modelfun.commons.RequestConfigHolder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * 用于配置http相关的参数，使用Apache的HttpClient作为具体实现类，外部封装Spring的RestTemplate
 *
 * @version 1.0
 * @date 2022/4/14 13:40
 */
@Slf4j
@Configuration
public class HttpConfig {

  @Bean
  HttpComponentsClientHttpRequestFactory httpRequestFactory() {
    HttpComponentsClientHttpRequestFactory httpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(createHttpClient());
    httpRequestFactory.setConnectTimeout(6000);
    httpRequestFactory.setReadTimeout(10000);
    httpRequestFactory.setHttpContextFactory(this::createHttpContext);
    return httpRequestFactory;
  }

  @Bean
  RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate
        .getMessageConverters()
        .forEach(
            converter -> {
              if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(StandardCharsets.UTF_8);
              }
            });
    restTemplate.setRequestFactory(httpRequestFactory());
    return restTemplate;
  }

  private HttpClient createHttpClient() {
    final RequestConfig DEFAULT_REQUEST_CONFIG =
        RequestConfig.custom()
            .setSocketTimeout(10000)
            .setConnectTimeout(6000)
            .setConnectionRequestTimeout(10000)
            .build();
    return HttpClients.custom()
        .setMaxConnTotal(400)
        // 配置每个host的最大连接，默认为2，如果不配置，相同的host请求并发只能到2
        .setMaxConnPerRoute(100)
        .setDefaultRequestConfig(DEFAULT_REQUEST_CONFIG)
        .setKeepAliveStrategy(this::keepAliveStrategy)
        .setRetryHandler(this::retryHandler)
        .build();
  }

  private long keepAliveStrategy(HttpResponse response, HttpContext context) {
    Args.notNull(response, "HTTP response");
    final HeaderElementIterator it =
        new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
    while (it.hasNext()) {
      final HeaderElement he = it.nextElement();
      final String param = he.getName();
      final String value = he.getValue();
      if (value != null && "timeout".equalsIgnoreCase(param)) {
        try {
          return Long.parseLong(value) * 1000;
        } catch (final NumberFormatException ignore) {
        }
      }
    }
    return DEFAULT_KEEP_ALIVE_TIME;
  }

  private boolean retryHandler(IOException exception, int executionCount, HttpContext context) {
    if (executionCount > MAX_RETRY) {
      log.warn("Maximum tries reached for client http pool ");
      return false;
    }

    if (exception instanceof NoHttpResponseException // NoHttpResponseException 重试
        || exception instanceof ConnectTimeoutException // 连接超时重试
    ) {
      log.warn("NoHttpResponseException on " + executionCount + " call");
      return true;
    }
    return false;
  }

  private HttpContext createHttpContext(HttpMethod method, URI uri) {
    RequestConfig requestConfig = RequestConfigHolder.get();
    if (requestConfig != null) {
      HttpContext context = HttpClientContext.create();
      context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig);
      return context;
    }
    return null;
  }
}
