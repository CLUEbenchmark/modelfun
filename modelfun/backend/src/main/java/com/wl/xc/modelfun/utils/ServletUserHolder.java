package com.wl.xc.modelfun.utils;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.security.component.SysAuthenticationToken;
import com.wl.xc.modelfun.config.security.component.SysUserDetail;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.po.SysUserPO;
import java.util.Collections;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @version 1.0
 * @date 2022/4/13 10:48
 */
public class ServletUserHolder {


  public static LoginUserInfo getUserByContext() {
    SecurityContext context = SecurityContextHolder.getContext();
    Authentication authentication = context.getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new BusinessIllegalStateException("用户未登录", 401);
    }
    SysAuthenticationToken userInfo = (SysAuthenticationToken) authentication;
    SysUserDetail principal = (SysUserDetail) userInfo.getPrincipal();
    SysUserPO sysUser = principal.getSysUser();
    LoginUserInfo info = new LoginUserInfo();
    info.setUserId(sysUser.getId());
    info.setUserName(sysUser.getUserName());
    info.setUserPhone(sysUser.getUserPhone());
    info.setUid(principal.getUid());
    return info;
  }

  public static void setUserToContext(LoginUserInfo userInfo) {
    SecurityContext context = SecurityContextHolder.getContext();
    SysUserDetail principal =
        new SysUserDetail(userInfo.getUserPhone(), null, Collections.emptyList());
    principal.setUid(userInfo.getUid());
    SysUserPO sysUser = new SysUserPO();
    sysUser.setUserName(userInfo.getUserName());
    sysUser.setUserPhone(userInfo.getUserPhone());
    sysUser.setId(userInfo.getUserId());
    principal.setSysUser(sysUser);
    SysAuthenticationToken authenticationToken =
        new SysAuthenticationToken(principal, null, Collections.emptyList());
    context.setAuthentication(authenticationToken);
  }
}
