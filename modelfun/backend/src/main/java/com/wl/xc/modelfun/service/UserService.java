package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.req.LoginReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;

/**
 * 用户相关的服务类接口
 *
 * @version 1.0
 * @date 2022/6/14 10:22
 */
public interface UserService {

  /**
   * 用户注册
   *
   * @param req 注册信息
   * @return 注册结果
   */
  ResultVo<Boolean> register(LoginReq req);
}
