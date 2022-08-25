package com.wl.xc.modelfun.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * @version 1.0
 * @date 2022/4/14 10:39
 */
public class MfLogoutSuccessHandler implements LogoutSuccessHandler {

  private ObjectMapper objectMapper;

  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    ResultVo<String> success = ResultVo.createSuccess("登出成功");
    response.setContentType("application/json;charset=utf-8");
    PrintWriter writer = response.getWriter();
    writer.write(objectMapper.writeValueAsString(success));
    writer.flush();
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
