package com.wl.xc.modelfun.commons.methods;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * @version 1.0
 * @date 2022/5/13 10:11
 */
public class JwtTokenMethods {

  public static List<GrantedAuthority> getAuthoritiesFromPayload(List<String> authorities) {
    if (authorities == null || authorities.isEmpty()) {
      return new ArrayList<>();
    }
    List<GrantedAuthority> grantedAuthorities = new ArrayList<>(authorities.size());
    for (String authority : authorities) {
      SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority);
      grantedAuthorities.add(grantedAuthority);
    }
    return grantedAuthorities;
  }
}
