package com.wl.xc.modelfun.config.security.component;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.11.2 14:32
 */
public class WebRequestAuthenticationDetails {

  private String remoteAddress;
  private String method;

  public WebRequestAuthenticationDetails() {
  }

  public WebRequestAuthenticationDetails build(HttpServletRequest request) {
    this.remoteAddress = request.getRemoteHost();
    this.method = request.getMethod();
    return this;
  }

  public String getRemoteAddress() {
    return remoteAddress;
  }

  public WebRequestAuthenticationDetails setRemoteAddress(String remoteAddress) {
    this.remoteAddress = remoteAddress;
    return this;
  }

  public String getMethod() {
    return method;
  }

  public WebRequestAuthenticationDetails setMethod(String method) {
    this.method = method;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebRequestAuthenticationDetails that = (WebRequestAuthenticationDetails) o;
    return Objects.equals(remoteAddress, that.remoteAddress) &&
        Objects.equals(method, that.method);
  }

  @Override
  public int hashCode() {
    return Objects.hash(remoteAddress, method);
  }
}
