package com.wl.xc.modelfun.config.security.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.config.security.component.SysAuthenticationToken;
import com.wl.xc.modelfun.entities.model.LoginUser;
import com.wl.xc.modelfun.utils.RequestUtil;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 支持多种格式传参的登录验证，正常来说filter中最好不使用request.getParameter和request.getInputStream等 会读取流的操作，因为会导致流关闭。
 * <p>
 * 但是由于是验证登录的，验证成功也没有后续业务，失败则直接返回失败，所以这里使用没有问题。
 * <p>
 * 如果配置了continueChainBeforeSuccessfulAuthentication=true，还有后续业务，则建议这里用自定义的RequestWrapper重新包装流。
 *
 * @version 1.0
 * @date 2020.10.19 21:20
 */
public class CustomerAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private static final String FORM_CONTENT = "application/x-www-form-urlencoded";
  private static final String JSON_CONTENT = "application/json";
  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final ThreadLocal<LoginUser> userLocal = new ThreadLocal<>();
  private final ObjectMapper mapper;
  private boolean postOnly = true;


  private static final Logger logger = LoggerFactory.getLogger(CustomerAuthenticationFilter.class);

  public CustomerAuthenticationFilter(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {
    try {
      if (postOnly && !request.getMethod().equals("POST")) {
        throw new AuthenticationServiceException(
            "Authentication method not supported: " + request.getMethod());
      }
      String username = obtainUsername(request);
      String password = obtainPassword(request);

      if (username == null) {
        username = "";
      }

      if (password == null) {
        password = "";
      }

      username = username.trim();
      SysAuthenticationToken authRequest = new SysAuthenticationToken(
          username, password);
      setDetails(request, authRequest);
      return getAuthenticationManager().authenticate(authRequest);
    } finally {
      userLocal.remove();
    }
  }

  @Override
  protected String obtainPassword(HttpServletRequest request) {
    if (Objects.equals(request.getContentType(), FORM_CONTENT)) {
      return super.obtainPassword(request);
    }
    if (Objects.equals(request.getContentType(), JSON_CONTENT)) {
      return getPasswordFromBody(request);
    }
    return null;
  }

  private String getPasswordFromBody(HttpServletRequest request) {
    if (userLocal.get() != null) {
      return userLocal.get().getPassword();
    }
    if (!getUserFromBody(request)) {
      return null;
    }
    return userLocal.get().getPassword();
  }

  @Override
  protected String obtainUsername(HttpServletRequest request) {
    if (Objects.equals(request.getContentType(), FORM_CONTENT)) {
      return super.obtainUsername(request);
    }
    if (Objects.equals(request.getContentType(), JSON_CONTENT)) {
      return getUsernameFromBody(request);
    }
    return null;
  }

  private boolean getUserFromBody(HttpServletRequest request) {
    String encoding = getEncoding(request);
    String requestBody = RequestUtil.getRequestBody(request, encoding);
    try {
      JsonNode jsonNode = mapper.readTree(requestBody);
      String username = jsonNode.get(getUsernameParameter()).textValue();
      String password = jsonNode.get(getPasswordParameter()).textValue();
      LoginUser loginUser = new LoginUser().setUsername(username).setPassword(password);
      userLocal.set(loginUser);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String getUsernameFromBody(HttpServletRequest request) {
    if (userLocal.get() != null) {
      return userLocal.get().getUsername();
    }
    if (!getUserFromBody(request)) {
      return null;
    }
    return userLocal.get().getUsername();
  }

  public String getEncoding(HttpServletRequest request) {
    String encoding = request.getCharacterEncoding();
    if (encoding != null && !encoding.isEmpty()) {
      return encoding;
    }
    String contentType = request.getContentType();
    if (contentType == null || contentType.isEmpty()) {
      return DEFAULT_ENCODING;
    }
    String[] split = contentType.split(":");
    for (String s : split) {
      if (s.toLowerCase().startsWith("charset")) {
        String[] charset = s.split("=");
        if (charset.length > 1) {
          encoding = charset[1];
          break;
        }
      }
    }
    if (encoding == null) {
      encoding = DEFAULT_ENCODING;
    }
    return encoding;
  }

  public void setPostOnly(boolean postOnly) {
    this.postOnly = postOnly;
  }
}
