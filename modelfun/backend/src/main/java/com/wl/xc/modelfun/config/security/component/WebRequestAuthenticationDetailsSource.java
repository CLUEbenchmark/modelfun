package com.wl.xc.modelfun.config.security.component;

import javax.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.11.2 14:29
 */
public class WebRequestAuthenticationDetailsSource implements
    AuthenticationDetailsSource<HttpServletRequest, WebRequestAuthenticationDetails> {


  @Override
  public WebRequestAuthenticationDetails buildDetails(HttpServletRequest context) {
    return new WebRequestAuthenticationDetails().build(context);
  }
}
