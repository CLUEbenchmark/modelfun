package com.wl.xc.modelfun.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.dto.IntegrateCallbackDTO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import com.wl.xc.modelfun.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文本任务集成回调处理类
 *
 * @version 1.0
 * @date 2022/6/29 14:03
 */
@Slf4j
@Service
public class IntegrationCallbackService implements AlgorithmCallbackService {

  private IntegrationService integrationService;

  private ObjectMapper objectMapper;

  @Override
  public CallBackAction getAction() {
    return CallBackAction.INTEGRATION;
  }

  @Override
  public ResultVo<Boolean> callback(String body) {
    IntegrateCallbackDTO integrateCallbackDTO;
    try {
      integrateCallbackDTO = objectMapper.readValue(body, IntegrateCallbackDTO.class);
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException("回调请求体解析失败", e);
    }
    return integrationService.saveIntegrationAsync(integrateCallbackDTO);
  }

  @Autowired
  public void setIntegrationService(IntegrationService integrationService) {
    this.integrationService = integrationService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
