package com.wl.xc.modelfun.config.security.handler;

import com.wl.xc.modelfun.entities.vo.ResultVo;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.10.21 19:01
 */
public class JsonAuthenticationFailHandler implements AuthenticationFailureHandler {

  private static final Logger log = LoggerFactory.getLogger(JsonAuthenticationFailHandler.class);

  private static final String DEFAULT_ENCODING = "utf-8";

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {
    response.setContentType("application/json");
    response.setCharacterEncoding(DEFAULT_ENCODING);
    ResultVo<Object> resultDto;
    if (exception instanceof InternalAuthenticationServiceException) {
      response.setStatus(500);
      resultDto = ResultVo.create(exception.getMessage(), 500, false, null);
    } else {
      response.setStatus(200);
      resultDto = ResultVo.create("账号密码错误！", 403, false, null);
    }
    try (PrintWriter writer = response.getWriter()) {
      writer.println(parse(resultDto));
      writer.flush();
    } catch (Exception e) {
      log.error("error:", e);
    }
  }

  private String parse(ResultVo<Object> resultDto) {
    String jsonStrFormat = "{\"code\": %d, \"msg\": \"%s\", \"success\": false}";
    return String.format(jsonStrFormat, resultDto.getCode(), resultDto.getMsg());
  }
}
