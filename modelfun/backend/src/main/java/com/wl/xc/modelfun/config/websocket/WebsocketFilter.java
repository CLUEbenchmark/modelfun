package com.wl.xc.modelfun.config.websocket;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.TOKEN_PREFIX;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.WS_TOKEN_PAYLOAD;
import static com.wl.xc.modelfun.commons.methods.JwtTokenMethods.getAuthoritiesFromPayload;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.properties.JWTProperties;
import com.wl.xc.modelfun.config.security.component.SysAuthenticationToken;
import com.wl.xc.modelfun.entities.dto.PayloadDTO;
import com.wl.xc.modelfun.service.JwtTokenService;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @version 1.0
 * @date 2022/5/12 16:44
 */
@Slf4j
public class WebsocketFilter implements Filter {

  private static final String JS_UNDEFINED = "undefined";

  private static final String ALREADY_FILTER = "WEBSOCKET_ALREADY_FILTER";

  private JwtTokenService jwtTokenService;

  private JWTProperties jwtProperties;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Filter.super.init(filterConfig);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    Object attribute = request.getAttribute(ALREADY_FILTER);
    if (attribute != null && (boolean) attribute) {
      chain.doFilter(request, response);
      return;
    }
    if (!(request instanceof HttpServletRequest)) {
      chain.doFilter(request, response);
      return;
    }
    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    String upgrade = httpServletRequest.getHeader("upgrade");
    if (StringUtils.isBlank(upgrade) || !upgrade.equalsIgnoreCase("websocket")) {
      chain.doFilter(request, response);
      return;
    }
    try {
      httpServletRequest.setAttribute(ALREADY_FILTER, true);
      String token = httpServletRequest.getParameter("token");
      log.debug("[WebsocketFilter.doFilter] {}", token);
      if (StringUtils.isBlank(token) || JS_UNDEFINED.equals(token)) {
        throw new BusinessIllegalStateException("token is null");
      }
      PayloadDTO payloadDTO = jwtTokenService.verifyTokenByHMAC(token.substring(TOKEN_PREFIX.length()),
          jwtProperties.getSecret());
      SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
      SysAuthenticationToken authenticationToken = new SysAuthenticationToken(payloadDTO.getUsername(), null,
          getAuthoritiesFromPayload(payloadDTO.getAuthorities()));
      authenticationToken.setDetails(payloadDTO);
      emptyContext.setAuthentication(authenticationToken);
      SecurityContextHolder.setContext(emptyContext);
      request.setAttribute(WS_TOKEN_PAYLOAD, payloadDTO);
      chain.doFilter(request, response);
    } finally {
      SecurityContextHolder.clearContext();
      httpServletRequest.removeAttribute(ALREADY_FILTER);
    }
  }

  @Override
  public void destroy() {
    Filter.super.destroy();
  }

  @Autowired
  public void setJwtTokenService(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }

  @Autowired
  public void setJwtProperties(JWTProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }
}
