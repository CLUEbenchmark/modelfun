package com.wl.xc.modelfun.config.security.component;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/3/31 16:43
 */
public class CustomerAuthenticationProvider extends DaoAuthenticationProvider {

  @Override
  protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
      UserDetails user) {
    Authentication result = super.createSuccessAuthentication(principal, authentication, user);
    SysAuthenticationToken token = new SysAuthenticationToken(result.getPrincipal(),
        result.getCredentials(), result.getAuthorities());
    token.setDetails(result.getDetails());
    token.setSysUser(((SysUserDetail) user).getSysUser());
    return token;
  }
}
