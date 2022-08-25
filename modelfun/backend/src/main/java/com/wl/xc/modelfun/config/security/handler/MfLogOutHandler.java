package com.wl.xc.modelfun.config.security.handler;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_PREFIX_NAME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.TOKEN_PREFIX;

import com.wl.xc.modelfun.config.properties.JWTProperties;
import com.wl.xc.modelfun.entities.dto.PayloadDTO;
import com.wl.xc.modelfun.service.JwtTokenService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * @version 1.0
 * @date 2022/4/14 10:22
 */
public class MfLogOutHandler implements LogoutHandler {

  private StringRedisTemplate stringRedisTemplate;

  private JwtTokenService tokenService;

  private JWTProperties jwtProperties;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return;
    }
    PayloadDTO token = getTokenFromRequest(request);
    if (token == null) {
      return;
    }
    String jti = token.getJti();
    stringRedisTemplate.delete(SESSION_PREFIX_NAME + jti);
  }

  private PayloadDTO getTokenFromRequest(HttpServletRequest request) {
    String tokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (StringUtils.isBlank(tokenHeader) || !tokenHeader.startsWith(TOKEN_PREFIX)) {
      return null;
    }
    PayloadDTO payload;
    try {
      payload = tokenService.verifyTokenByHMAC(tokenHeader.substring(TOKEN_PREFIX.length()),
          jwtProperties.getSecret());
    } catch (Exception e) {
      return null;
    }
    return payload;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setTokenService(JwtTokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Autowired
  public void setJwtProperties(JWTProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }
}
