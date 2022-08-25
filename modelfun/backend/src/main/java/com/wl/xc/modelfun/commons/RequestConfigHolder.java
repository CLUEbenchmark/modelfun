package com.wl.xc.modelfun.commons;

import org.apache.http.client.config.RequestConfig;

/**
 * @version 1.0
 * @date 2022/4/21 9:46
 */
public class RequestConfigHolder {

  private static final ThreadLocal<RequestConfig> threadLocal = new ThreadLocal<>();

  public static void bind(RequestConfig requestConfig) {
    threadLocal.set(requestConfig);
  }

  public static RequestConfig get() {
    return threadLocal.get();
  }

  public static void clear() {
    threadLocal.remove();
  }
}
