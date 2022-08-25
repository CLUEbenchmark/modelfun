package com.wl.xc.modelfun.config.security.handler;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.10.21 19:48
 */
@Slf4j
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final String errorMsg = "{\"code\": 401, \"msg\": \"权限认证失败\", \"success\": false}";

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {
    log.error("[JsonAuthenticationEntryPoint.commence] 权限验证失败：{}", authException.getMessage());
    response.setContentType("application/json; charset=UTF-8");
    response.setStatus(200);
    PrintWriter writer = response.getWriter();
    writer.println(errorMsg);
    writer.flush();
    writer.close();
  }


}
