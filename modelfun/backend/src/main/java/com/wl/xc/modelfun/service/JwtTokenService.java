package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.dto.PayloadDTO;

/**
 * token对应生成和解析的服务类
 *
 * @version 1.0
 * @date 2022/4/12 18:39
 */
public interface JwtTokenService {

  /**
   * 使用HMAC对称加密算法生成token
   */
  String generateTokenByHMAC(PayloadDTO payloadDTO, String secret) throws BusinessIllegalStateException;


  /**
   * 验证令牌
   */
  PayloadDTO verifyTokenByHMAC(String token, String secret);

}
