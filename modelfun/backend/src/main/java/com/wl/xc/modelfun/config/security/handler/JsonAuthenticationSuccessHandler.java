package com.wl.xc.modelfun.config.security.handler;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_PREFIX_NAME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_TIME_OUT;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.TOKEN_PREFIX;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.ResponseCodeEnum;
import com.wl.xc.modelfun.config.properties.JWTProperties;
import com.wl.xc.modelfun.config.security.component.SysAuthenticationToken;
import com.wl.xc.modelfun.entities.dto.PayloadDTO;
import com.wl.xc.modelfun.entities.po.SysUserPO;
import com.wl.xc.modelfun.entities.vo.LoginSuccessVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.JwtTokenService;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * @version 1.0
 * @author: Fan
 * @date 2020.10.21 18:28
 */
public class JsonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private static final String DEFAULT_ENCODING = "utf-8";

  private final ObjectMapper objectMapper;

  private JwtTokenService jwtTokenService;

  private JWTProperties jwtProperties;

  private StringRedisTemplate stringRedisTemplate;

  public JsonAuthenticationSuccessHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setContentType("application/json");
    response.setCharacterEncoding(DEFAULT_ENCODING);
    PrintWriter writer = response.getWriter();
    ResultVo<?> resultVo;
    SysAuthenticationToken authenticationToken = (SysAuthenticationToken) authentication;
    PayloadDTO payloadDTO = new PayloadDTO();
    payloadDTO.setAuthorities(
        authenticationToken.getAuthorities().stream()
            .map(Objects::toString)
            .collect(Collectors.toList()));
    payloadDTO.setIat(System.currentTimeMillis());
    payloadDTO.setExp(payloadDTO.getIat() + SESSION_TIME_OUT * 1000);
    SysUserPO sysUser = authenticationToken.getSysUser();
    payloadDTO.setUserId(sysUser.getId());
    payloadDTO.setUsername(sysUser.getUserName());
    payloadDTO.setUserPhone(sysUser.getUserPhone());
    payloadDTO.setJti(UUID.randomUUID().toString());
    String token = jwtTokenService.generateTokenByHMAC(payloadDTO, jwtProperties.getSecret());
    LoginSuccessVO vo = new LoginSuccessVO();
    vo.setId(sysUser.getId());
    vo.setUsername(sysUser.getUserName());
    vo.setUserPhone(sysUser.getUserPhone());
    vo.setToken(TOKEN_PREFIX + token);
    stringRedisTemplate.opsForValue()
        .set(SESSION_PREFIX_NAME + payloadDTO.getJti(),
            payloadDTO.getUserPhone(), SESSION_TIME_OUT, TimeUnit.SECONDS);
    resultVo = ResultVo.create(ResponseCodeEnum.SUCCESS, "登录成功", true, vo);
    writer.write(objectMapper.writeValueAsString(resultVo));
    writer.flush();
    writer.close();
  }

  @Autowired
  public void setJwtTokenService(JwtTokenService jwtTokenService) {
    this.jwtTokenService = jwtTokenService;
  }

  @Autowired
  public void setJwtProperties(JWTProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }
}
