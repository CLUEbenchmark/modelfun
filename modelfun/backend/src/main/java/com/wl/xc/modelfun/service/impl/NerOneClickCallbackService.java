package com.wl.xc.modelfun.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.entities.dto.OneClickCallbackDTO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import com.wl.xc.modelfun.service.IntelligentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ner一键标注的回调处理类
 *
 * @version 1.0
 * @date 2022/6/29 14:43
 */
@Slf4j
@Service
public class NerOneClickCallbackService implements AlgorithmCallbackService {

  private IntelligentService intelligentService;

  private ObjectMapper objectMapper;

  @Override
  public CallBackAction getAction() {
    return CallBackAction.NER_ONE_CLICK;
  }

  @Override
  public ResultVo<Boolean> callback(String body) {
    OneClickCallbackDTO dto;
    try {
      dto = objectMapper.readValue(body, OneClickCallbackDTO.class);
    } catch (JsonProcessingException e) {
      log.error("[NerOneClickCallbackService.callback]", e);
      return ResultVo.create("回调请求体解析失败", -1, false, false);
    }
    return intelligentService.oneClickCallback(dto);
  }

  @Autowired
  public void setIntelligentService(IntelligentService intelligentService) {
    this.intelligentService = intelligentService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
