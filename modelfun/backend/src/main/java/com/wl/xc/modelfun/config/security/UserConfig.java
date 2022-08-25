package com.wl.xc.modelfun.config.security;

import com.wl.xc.modelfun.config.security.component.SysUserDetail;
import com.wl.xc.modelfun.entities.po.SysUserPO;
import com.wl.xc.modelfun.service.SysUserService;
import javax.annotation.Resource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2020.10.18 21:18
 */
@Component
public class UserConfig implements UserDetailsService {

  @Resource
  private SysUserService sysUserService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    SysUserPO sysUser = sysUserService.findUserByPhone(username);
    if (sysUser == null) {
      throw new UsernameNotFoundException("user:{}" + username + "not found");
    }
    SysUserDetail detail = new SysUserDetail(sysUser.getUserPhone(), sysUser.getUserPassword(),
        AuthorityUtils.createAuthorityList("1", "2"));
    sysUser.setUserPassword(null);
    detail.setSysUser(sysUser);
    return detail;
  }
}
