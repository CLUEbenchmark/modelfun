package com.wl.xc.modelfun.tasks.rule.constructors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.entities.model.LabelFunctionRule;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.rule.RuleDescribe;
import com.wl.xc.modelfun.tasks.rule.RuleHandlerConstructor;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import com.wl.xc.modelfun.tasks.rule.handlers.LabelFunctionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @version 1.0
 * @date 2022/4/20 14:39
 */
@Component
public class LabelFunctionHandlerConstructor implements RuleHandlerConstructor {

  private ObjectMapper objectMapper;

  private RestTemplate restTemplate;

  private AlgorithmProperties algorithmProperties;

  private OssService ossService;

  private DatasetDetailService datasetDetailService;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private LabelInfoService labelInfoService;

  @Override
  public RuleType getRuleType() {
    return RuleType.LABEL_FUNCTION;
  }

  @Override
  public RuleTaskHandler createHandler(RuleDescribe ruleDescribe) {
    LabelFunctionRule labelFunctionRule;
    try {
      labelFunctionRule = objectMapper.readValue(ruleDescribe.getMetadata(), LabelFunctionRule.class);
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException(e);
    }
    DatasetDetailPO trainDataset = datasetDetailService.selectByTaskIdAndType(
        ruleDescribe.getTaskId(), DatasetType.UNLABELED.getType());
    LabelFunctionHandler handler = new LabelFunctionHandler(restTemplate, labelFunctionRule,
        algorithmProperties, ruleDescribe);
    DatasetDetailPO testDataset = datasetDetailService.selectByTaskIdAndType(
        ruleDescribe.getTaskId(), DatasetType.TEST_UN_SHOW.getType());

    DatasetDetailPO valDataset = datasetDetailService.selectByTaskIdAndType(
        ruleDescribe.getTaskId(), DatasetType.TEST_SHOW.getType());
    handler.setTestDataPath(ossService.getUrlSigned(testDataset.getFileAddress(), 1000 * 60 * 60));
    handler.setValDataPath(ossService.getUrlSigned(valDataset.getFileAddress(), 1000 * 60 * 60));
    handler.setTrainDataPath(ossService.getUrlSigned(trainDataset.getFileAddress(), 1000 * 60 * 60));
    handler.setTestDataService(testDataService);
    handler.setUnlabelDataService(unlabelDataService);
    handler.setLabelInfoService(labelInfoService);
    return handler;
  }

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Autowired
  public void setAlgorithmProperties(AlgorithmProperties algorithmProperties) {
    this.algorithmProperties = algorithmProperties;
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
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
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
