package com.wl.xc.modelfun.websocket;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * A {@link javax.websocket.server.ServerEndpointConfig.Configurator} for initializing {@link ServerEndpoint}-annotated
 * classes through Spring.
 *
 * <p>
 * <pre class="code">
 * &#064;ServerEndpoint(value = "/echo", configurator = SpringConfigurator.class)
 * public class EchoEndpoint {
 *     // ...
 * }
 * </pre>
 *
 * @author Rossen Stoyanchev
 * @see ServerEndpointExporter
 * @since 4.0
 */
public class SpringConfigurator extends Configurator {

  private static final String NO_VALUE = ObjectUtils.identityToString(new Object());

  private static final Log logger = LogFactory.getLog(
      org.springframework.web.socket.server.standard.SpringConfigurator.class);

  private static final Map<String, Map<Class<?>, String>> cache =
      new ConcurrentHashMap<>();


  @SuppressWarnings("unchecked")
  @Override
  public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
    WebApplicationContext wac = WebSocketApplicationContext.getCurrentWebApplicationContext();
    if (wac == null) {
      String message = "Failed to find the root WebApplicationContext. Was ContextLoaderListener not used?";
      logger.error(message);
      throw new IllegalStateException(message);
    }

    String beanName = ClassUtils.getShortNameAsProperty(endpointClass);
    if (wac.containsBean(beanName)) {
      T endpoint = wac.getBean(beanName, endpointClass);
      if (logger.isTraceEnabled()) {
        logger.trace("Using @ServerEndpoint singleton " + endpoint);
      }
      return endpoint;
    }

    Component ann = AnnotationUtils.findAnnotation(endpointClass, Component.class);
    if (ann != null && wac.containsBean(ann.value())) {
      T endpoint = wac.getBean(ann.value(), endpointClass);
      if (logger.isTraceEnabled()) {
        logger.trace("Using @ServerEndpoint singleton " + endpoint);
      }
      return endpoint;
    }

    beanName = getBeanNameByType(wac, endpointClass);
    if (beanName != null) {
      return (T) wac.getBean(beanName);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("Creating new @ServerEndpoint instance of type " + endpointClass);
    }
    return wac.getAutowireCapableBeanFactory().createBean(endpointClass);
  }

  @Override
  public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
    super.modifyHandshake(sec, request, response);
  }

  @Nullable
  private String getBeanNameByType(WebApplicationContext wac, Class<?> endpointClass) {
    String wacId = wac.getId();

    Map<Class<?>, String> beanNamesByType = cache.get(wacId);
    if (beanNamesByType == null) {
      beanNamesByType = new ConcurrentHashMap<>();
      cache.put(wacId, beanNamesByType);
    }

    if (!beanNamesByType.containsKey(endpointClass)) {
      String[] names = wac.getBeanNamesForType(endpointClass);
      if (names.length == 1) {
        beanNamesByType.put(endpointClass, names[0]);
      } else {
        beanNamesByType.put(endpointClass, NO_VALUE);
        if (names.length > 1) {
          throw new IllegalStateException("Found multiple @ServerEndpoint's of type [" +
              endpointClass.getName() + "]: bean names " + Arrays.asList(names));
        }
      }
    }

    String beanName = beanNamesByType.get(endpointClass);
    return (NO_VALUE.equals(beanName) ? null : beanName);
  }

}