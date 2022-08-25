package com.wl.xc.modelfun.tasks.rule.constructors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.model.ExpertRule;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.tasks.rule.RuleDescribe;
import com.wl.xc.modelfun.tasks.rule.RuleHandlerConstructor;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import com.wl.xc.modelfun.tasks.rule.handlers.ExpertHandler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022.4.16 11:51
 */
@Component
public class ExpertHandlerConstructor implements RuleHandlerConstructor {

  private ObjectMapper objectMapper;

  private OssService ossService;

  private LabelInfoService labelInfoService;

  @Override
  public RuleType getRuleType() {
    return RuleType.EXPERT;
  }

  @Override
  public RuleTaskHandler createHandler(RuleDescribe ruleDescribe) {
    List<ExpertRule> expertRules;
    try {
      expertRules = objectMapper.readValue(ruleDescribe.getMetadata(), new TypeReference<List<ExpertRule>>() {
      });
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException(e);
    }
    ExpertHandler handler = new ExpertHandler(expertRules, ruleDescribe.getTaskId());
    handler.setOssService(ossService);
    handler.setLabelInfoService(labelInfoService);
    return handler;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }
}
