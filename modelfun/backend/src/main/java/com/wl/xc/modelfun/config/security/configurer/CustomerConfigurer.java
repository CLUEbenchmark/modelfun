package com.wl.xc.modelfun.config.security.configurer;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_PREFIX_NAME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_TIME_OUT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.config.security.component.WebRequestAuthenticationDetailsSource;
import com.wl.xc.modelfun.config.security.filter.CustomerAuthenticationFilter;
import com.wl.xc.modelfun.config.security.handler.JsonAuthenticationFailHandler;
import com.wl.xc.modelfun.config.security.handler.JsonAuthenticationSuccessHandler;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * @version 1.0
 * @date 2020.10.19 21:35
 */
public class CustomerConfigurer<H extends HttpSecurityBuilder<H>> extends
    AbstractAuthenticationFilterConfigurer<H, CustomerConfigurer<H>, CustomerAuthenticationFilter> {

  private static final Logger log = LoggerFactory.getLogger(CustomerConfigurer.class);

  private final ObjectMapper objectMapper;

  public CustomerConfigurer(ObjectMapper objectMapper) {
    super(new CustomerAuthenticationFilter(objectMapper), null);
    this.objectMapper = objectMapper;
  }

  @Override
  protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
    return new AntPathRequestMatcher(loginProcessingUrl, "POST");
  }

  private void defaultConfig(H http) {
    successHandler(postProcess(new JsonAuthenticationSuccessHandler(objectMapper)));
    failureHandler(new JsonAuthenticationFailHandler());
    loginProcessingUrl("/doLogin");
    authenticationDetailsSource(new WebRequestAuthenticationDetailsSource());
  }

  @Override
  public void init(H http) throws Exception {
    super.init(http);
    defaultConfig(http);
  }

  @Override
  public void configure(H http) throws Exception {
    super.configure(http);
    SecurityContextRepository securityContextRepository = http.
        getSharedObject(SecurityContextRepository.class);
    //postProcess(securityContextRepository);
  }

  private static class RedisSessionAuthenticationStrategy implements SessionAuthenticationStrategy {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request,
        HttpServletResponse response) throws SessionAuthenticationException {
      if (authentication == null || !authentication.isAuthenticated()) {
        return;
      }
      String uuid = UUID.randomUUID().toString();
      String userDetail;
      try {
        userDetail = objectMapper.writeValueAsString(authentication);
      } catch (Exception e) {
        log.error("json 解析错误", e);
        return;
      }
      String key = SESSION_PREFIX_NAME + uuid;
      stringRedisTemplate.opsForValue().set(key, userDetail, SESSION_TIME_OUT, TimeUnit.SECONDS);
      request.setAttribute("isLogin", true);
      request.setAttribute("loginUid", uuid);
      Cookie cookie = new Cookie("UID", uuid);
      cookie.setPath("/");
      response.addCookie(cookie);
    }
  }
}
