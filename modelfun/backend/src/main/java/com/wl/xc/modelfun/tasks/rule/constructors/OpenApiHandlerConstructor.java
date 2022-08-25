package com.wl.xc.modelfun.tasks.rule.constructors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.model.OpenApiRule;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.rule.RuleDescribe;
import com.wl.xc.modelfun.tasks.rule.RuleHandlerConstructor;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import com.wl.xc.modelfun.tasks.rule.handlers.OpenApiHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * openapi handler 构造器
 *
 * @version 1.0
 * @date 2022/4/20 14:35
 */
@Component
public class OpenApiHandlerConstructor implements RuleHandlerConstructor {

  private ObjectMapper objectMapper;

  private RestTemplate restTemplate;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private LabelInfoService labelInfoService;

  @Override
  public RuleType getRuleType() {
    return RuleType.OPEN_API;
  }

  @Override
  public RuleTaskHandler createHandler(RuleDescribe ruleDescribe) {
    OpenApiRule openApiRule;
    try {
      openApiRule = objectMapper.readValue(ruleDescribe.getMetadata(), OpenApiRule.class);
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException(e);
    }
    OpenApiHandler apiHandler = new OpenApiHandler(ruleDescribe.getTaskId(), openApiRule, restTemplate);
    apiHandler.setTestDataService(testDataService);
    apiHandler.setUnlabelDataService(unlabelDataService);
    apiHandler.setLabelInfoService(labelInfoService);
    apiHandler.setObjectMapper(objectMapper);
    return apiHandler;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }
}
