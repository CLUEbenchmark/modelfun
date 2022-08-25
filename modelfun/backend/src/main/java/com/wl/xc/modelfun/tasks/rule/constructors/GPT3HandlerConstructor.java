package com.wl.xc.modelfun.tasks.rule.constructors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule;
import com.wl.xc.modelfun.service.GptCacheService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.rule.RuleDescribe;
import com.wl.xc.modelfun.tasks.rule.RuleHandlerConstructor;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import com.wl.xc.modelfun.tasks.rule.handlers.GPT3Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @version 1.0
 * @date 2022.4.16 11:10
 */
@Component
public class GPT3HandlerConstructor implements RuleHandlerConstructor {

  private AlgorithmProperties algorithmProperties;

  private FileUploadProperties fileUploadProperties;

  private RuleInfoService ruleInfoService;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private GptCacheService gptCacheService;

  private OssService ossService;

  private ObjectMapper objectMapper;

  private StringRedisTemplate stringRedisTemplate;

  private RestTemplate restTemplate;

  private LabelInfoService labelInfoService;

  @Override
  public RuleType getRuleType() {
    return RuleType.GPT3;
  }

  @Override
  public RuleTaskHandler createHandler(RuleDescribe ruleDescribe) {
    BuiltinModelRule modelRule = null;
    try {
      modelRule = objectMapper.readValue(ruleDescribe.getMetadata(), BuiltinModelRule.class);
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException("规则格式错误", e);
    }
    GPT3Handler handler = new GPT3Handler(ruleDescribe.getTaskId(), ruleDescribe.getRuleId(), modelRule);
    handler.setLabelInfoService(labelInfoService);
    handler.setAlgorithmProperties(algorithmProperties);
    handler.setRuleInfoService(ruleInfoService);
    handler.setTestDataService(testDataService);
    handler.setUnlabelDataService(unlabelDataService);
    handler.setGptCacheService(gptCacheService);
    handler.setRestTemplate(restTemplate);
    handler.setStringRedisTemplate(stringRedisTemplate);
    handler.setOssService(ossService);
    handler.setFileUploadProperties(fileUploadProperties);
    handler.setObjectMapper(objectMapper);
    return handler;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setAlgorithmProperties(AlgorithmProperties algorithmProperties) {
    this.algorithmProperties = algorithmProperties;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
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
  public void setGptCacheService(GptCacheService gptCacheService) {
    this.gptCacheService = gptCacheService;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
}
