package com.wl.xc.modelfun.config.security.component;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wl.xc.modelfun.entities.po.SysUserPO;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * @version 1.0
 * @date 2020.11.1 13:11
 */
@JsonDeserialize(using = UserDeserializer.class)
public class SysUserDetail extends User {

  private SysUserPO sysUser;

  private String uid;

  public SysUserDetail(String username, String password,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, authorities);
  }

  public SysUserDetail(String username, String password, boolean enabled, boolean accountNonExpired,
      boolean credentialsNonExpired,
      boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
  }

  public void setSysUser(SysUserPO sysUser) {
    this.sysUser = sysUser;
  }

  public SysUserPO getSysUser() {
    return sysUser;
  }

  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }
}
