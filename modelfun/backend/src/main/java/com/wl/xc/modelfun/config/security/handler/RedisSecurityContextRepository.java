package com.wl.xc.modelfun.config.security.handler;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_PREFIX_NAME;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_TIME_OUT;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.TOKEN_PREFIX;
import static com.wl.xc.modelfun.commons.methods.JwtTokenMethods.getAuthoritiesFromPayload;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.config.properties.JWTProperties;
import com.wl.xc.modelfun.config.security.component.SysAuthenticationToken;
import com.wl.xc.modelfun.config.security.component.SysUserDetail;
import com.wl.xc.modelfun.entities.dto.PayloadDTO;
import com.wl.xc.modelfun.entities.po.SysUserPO;
import com.wl.xc.modelfun.service.JwtTokenService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * redis + token 实现的安全上下文存储
 * <p>
 * 先从token中获取用户信息，如果token解析失败，直接返回401，要求重新登录
 * <p>
 * 如果token解析成功，则判断对应redis中对应的用户信息是否过期，如果过期，则返回401，要求重新登录
 * <p>
 * 如果对应的用户信息没有过期，则将用户信息放入上下文中，允许请求
 *
 * @version 1.0
 * @author: Fan
 * @date 2020.10.30 15:40
 */
@Component
public class RedisSecurityContextRepository implements SecurityContextRepository {

  private static final Logger log = LoggerFactory.getLogger(RedisSecurityContextRepository.class);

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtTokenService tokenService;

  @Autowired
  private JWTProperties jwtProperties;

  @Override
  public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
    SecurityContext context = SecurityContextHolder.getContext();
    if (context != null
        && context.getAuthentication() != null
        && context.getAuthentication().isAuthenticated()) {
      return context;
    }
    PayloadDTO token;
    if ((token = getTokenFromRequest(requestResponseHolder.getRequest())) == null) {
      return SecurityContextHolder.createEmptyContext();
    }
    return containsKey(getRepositoryKey(token.getJti()))
        ? getContextFromToken(token)
        : SecurityContextHolder.createEmptyContext();
  }

  @Override
  public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
    Authentication authentication = context.getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return;
    }
    String uuid;
    boolean isLogin = Optional.ofNullable((Boolean) request.getAttribute("isLogin")).orElse(false);
    if (isLogin) {
      uuid = (String) request.getAttribute("loginUid");
    } else {
      PayloadDTO token = getTokenFromRequest(request);
      uuid = token == null ? null : token.getJti();
    }
    if (uuid == null) {
      uuid = UUID.randomUUID().toString();
    }
    String key = getRepositoryKey(uuid);
    if (containsKey(key)) {
      stringRedisTemplate.expire(key, SESSION_TIME_OUT, TimeUnit.SECONDS);
    }
  }

  @Override
  public boolean containsContext(HttpServletRequest request) {
    PayloadDTO payload = getTokenFromRequest(request);
    if (payload == null) {
      return false;
    }
    String key = getRepositoryKey(payload.getJti());
    return containsKey(key);
  }

  private boolean containsKey(String key) {
    return Optional.ofNullable(stringRedisTemplate.hasKey(key)).orElse(false);
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
      log.error("[RedisSecurityContextRepository.getTokenFromRequest] token解析失败。", e);
      return null;
    }
    return payload;
  }

  private String getRepositoryKey(String uid) {
    return SESSION_PREFIX_NAME + uid;
  }

  private SecurityContext getContextFromToken(PayloadDTO payload) {
    SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
    List<String> authorities = payload.getAuthorities();
    List<GrantedAuthority> grantedAuthorities = getAuthoritiesFromPayload(authorities);
    SysUserDetail principal = new SysUserDetail(payload.getUserPhone(), "", grantedAuthorities);
    SysUserPO po = new SysUserPO();
    po.setId(payload.getUserId());
    po.setUserName(payload.getUsername());
    po.setUserPhone(payload.getUserPhone());
    principal.setSysUser(po);
    principal.setUid(payload.getJti());
    SysAuthenticationToken token = new SysAuthenticationToken(principal, null, grantedAuthorities);
    emptyContext.setAuthentication(token);
    return emptyContext;
  }

  private boolean judge(JsonNode node) {
    JsonNode node1 = node.get("authority");
    return StrUtil.isNotBlank(node1.asText());
  }
}
