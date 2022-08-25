package com.wl.xc.modelfun.service.impl;

import com.wl.xc.modelfun.entities.po.SysUserPO;
import com.wl.xc.modelfun.entities.req.LoginReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.SysUserService;
import com.wl.xc.modelfun.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/6/14 10:23
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

  private PasswordEncoder passwordEncoder;

  private SysUserService sysUserService;

  @Override
  public ResultVo<Boolean> register(LoginReq req) {
    SysUserPO userPO = new SysUserPO();
    userPO.setUserName(req.getUsername().trim());
    userPO.setUserPhone(req.getUserPhone().trim());
    try {
      String passEncode = passwordEncoder.encode(req.getPassword().trim());
      userPO.setUserPassword(passEncode);
    } catch (Exception e) {
      log.error("[UserServiceImpl.register]", e);
      return ResultVo.create("当前资源紧张，请稍后重试！", -1, false, false);
    }
    sysUserService.save(userPO);
    return ResultVo.createSuccess(true);
  }

  @Autowired
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  @Autowired
  public void setSysUserService(SysUserService sysUserService) {
    this.sysUserService = sysUserService;
  }
}
