package com.wl.xc.modelfun.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.dto.PayloadDTO;
import com.wl.xc.modelfun.service.JwtTokenService;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/12 18:39
 */
@Slf4j
@Service
public class JwtTokenServiceImpl implements JwtTokenService {

  private ObjectMapper objectMapper;

  private final ConcurrentHashMap<String, MACSigner> signers = new ConcurrentHashMap<>();

  @Override
  public String generateTokenByHMAC(PayloadDTO payloadDTO, String secret) throws BusinessIllegalStateException {
    try {
      String payloadStr = objectMapper.writeValueAsString(payloadDTO);
      // 准备JWS-header
      JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS256)
          .type(JOSEObjectType.JWT).build();
      // 将负载信息装载到payload
      Payload payload = new Payload(payloadStr);
      // 封装header和payload到JWS对象
      JWSObject jwsObject = new JWSObject(jwsHeader, payload);
      // 获取HMAC签名器
      JWSSigner jwsSigner =
          signers.computeIfAbsent(
              secret,
              s -> {
                try {
                  return new MACSigner(s);
                } catch (KeyLengthException e) {
                  throw new BusinessIllegalStateException("秘钥长度不合法");
                }
              });
      // 签名
      jwsObject.sign(jwsSigner);
      return jwsObject.serialize();
    } catch (Exception e) {
      log.error("[JwtTokenServiceImpl.generateTokenByHMAC]", e);
      throw new BusinessIllegalStateException("生成token失败");
    }
  }

  @Override
  public PayloadDTO verifyTokenByHMAC(String token, String secret) {
    try {
      JWSObject jwsObject = JWSObject.parse(token);
      //创建HMAC验证器
      JWSVerifier jwsVerifier = new MACVerifier(secret);
      if (!jwsObject.verify(jwsVerifier)) {
        throw new BusinessIllegalStateException("token签名不合法!", 401);
      }
      String payload = jwsObject.getPayload().toString();
      return objectMapper.readValue(payload, PayloadDTO.class);
    } catch (BusinessIllegalStateException e) {
      throw e;
    } catch (Exception e) {
      log.error("[JwtTokenServiceImpl.verifyTokenByHMAC] token验证失败");
      throw new BusinessIllegalStateException("token验证失败!", 401);
    }
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
