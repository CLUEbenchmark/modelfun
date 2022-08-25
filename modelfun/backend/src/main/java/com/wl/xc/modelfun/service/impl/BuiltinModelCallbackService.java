package com.wl.xc.modelfun.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.dto.GPTCallbackDTO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import com.wl.xc.modelfun.service.LabelRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 内置模型的算法回调处理类
 *
 * @version 1.0
 * @date 2022/6/29 13:37
 */
@Slf4j
@Service
public class BuiltinModelCallbackService implements AlgorithmCallbackService {

  private LabelRuleService labelRuleService;

  private ObjectMapper objectMapper;

  @Override
  public CallBackAction getAction() {
    return CallBackAction.BUILTIN_MODEL;
  }

  @Override
  public ResultVo<Boolean> callback(String body) {
    GPTCallbackDTO callbackDTO;
    try {
      callbackDTO = objectMapper.readValue(body, GPTCallbackDTO.class);
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException("回调请求体解析失败", e);
    }
    log.info(
        "[LabelRuleController.saveGPTResultAsync] 收到GPT接口回调, taskId={}，ruleId={}, labels={}, detail={}",
        callbackDTO.getTaskId(),
        callbackDTO.getRecordId(),
        callbackDTO.getLabels(),
        callbackDTO.getDetail());
    return labelRuleService.saveGPTResultAsync(callbackDTO);
  }

  @Autowired
  public void setLabelRuleService(LabelRuleService labelRuleService) {
    this.labelRuleService = labelRuleService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
