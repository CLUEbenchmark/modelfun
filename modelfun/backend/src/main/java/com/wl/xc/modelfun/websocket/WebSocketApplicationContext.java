package com.wl.xc.modelfun.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * @version 1.0
 * @date 2022/5/12 16:24
 */
@Component
public class WebSocketApplicationContext implements ServletContextInitializer {

  private static final Logger log = LoggerFactory.getLogger(WebSocketApplicationContext.class);

  private WebApplicationContext context;

  private static volatile WebApplicationContext currentContext;

  private static final Map<ClassLoader, WebApplicationContext> currentContextPerThread =
      new ConcurrentHashMap<>(1);

  @Override
  public void onStartup(ServletContext servletContext) throws ServletException {
    if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) == null) {
      throw new IllegalStateException("WebApplicationContext not found!");
    }
    this.context = (WebApplicationContext) servletContext.getAttribute(
        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    servletContext.log("Initializing Spring root WebApplicationContext");
    if (log.isInfoEnabled()) {
      log.info("Root WebApplicationContext: inject started");
    }
    long startTime = System.currentTimeMillis();

    try {
      ClassLoader ccl = Thread.currentThread().getContextClassLoader();
      if (ccl == ContextLoader.class.getClassLoader()) {
        currentContext = this.context;
      } else if (ccl != null) {
        currentContextPerThread.put(ccl, this.context);
      }
    } catch (RuntimeException | Error ex) {
      log.error("Context initialization failed", ex);
      throw ex;
    }
  }

  public static WebApplicationContext getCurrentWebApplicationContext() {
    ClassLoader ccl = Thread.currentThread().getContextClassLoader();
    if (ccl != null) {
      WebApplicationContext ccpt = currentContextPerThread.get(ccl);
      if (ccpt != null) {
        return ccpt;
      }
    }
    return currentContext;
  }
}
