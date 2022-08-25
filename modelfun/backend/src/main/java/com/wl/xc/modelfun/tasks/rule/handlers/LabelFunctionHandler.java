package com.wl.xc.modelfun.tasks.rule.handlers;

import com.wl.xc.modelfun.commons.RequestConfigHolder;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.entities.dto.LabelFunctionDTO;
import com.wl.xc.modelfun.entities.model.LabelFunctionRule;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.rule.RuleDescribe;
import com.wl.xc.modelfun.utils.PageUtil;
import com.wl.xc.modelfun.utils.PythonCheckUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @version 1.0
 * @date 2022/4/20 14:27
 */
public class LabelFunctionHandler extends AbstractOutLabelHandler {

  private static final Logger log = LoggerFactory.getLogger(LabelFunctionHandler.class);

  private final RestTemplate restTemplate;

  private final LabelFunctionRule labelFunctionRule;

  private final AlgorithmProperties algorithmProperties;

  private final RuleDescribe ruleDescribe;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private String testDataPath;

  private String valDataPath;

  private String trainDataPath;

  private Map<String, Integer> testLabelMap = new HashMap<>(2000);

  private Map<String, Integer> trainLabelMap = new HashMap<>(10000);

  public LabelFunctionHandler(
      RestTemplate restTemplate,
      LabelFunctionRule labelFunctionRule,
      AlgorithmProperties algorithmProperties,
      RuleDescribe ruleDescribe) {
    super(ruleDescribe.getTaskId());
    this.restTemplate = restTemplate;
    this.labelFunctionRule = labelFunctionRule;
    this.algorithmProperties = algorithmProperties;
    this.ruleDescribe = ruleDescribe;
  }

  @Override
  public RuleType getRuleType() {
    return RuleType.LABEL_FUNCTION;
  }

  @Override
  public void init() {
    super.init();
    if (labelFunctionRule.getFunctionName().equals("lf")) {
      labelFunctionRule.setFunctionName("lf" + System.currentTimeMillis());
    }
    String reWriteBody = PythonCheckUtil.reWriteBody(labelFunctionRule.getFunctionBody());
    // 获取测试集的标签
    LabelFunctionDTO testDto = new LabelFunctionDTO();
    testDto.setContent(reWriteBody);
    testDto.setName(labelFunctionRule.getFunctionName());
    testDto.setDataPath(testDataPath);
    // 获取测试集标签
    List<Integer> testLabelResult = getLabelResult(testDto);
    getTestDataMap(testLabelResult);
    log.info("testLabelResult: " + testLabelResult.size());
    // 获取验证集集标签
    testDto.setDataPath(valDataPath);
    List<Integer> valPath = getLabelResult(testDto);
    getValDataMap(valPath);
    // 获取未标注集的标签
    LabelFunctionDTO trainDto = new LabelFunctionDTO();
    trainDto.setContent(reWriteBody);
    trainDto.setName(labelFunctionRule.getFunctionName());
    trainDto.setDataPath(trainDataPath);
    // 获取未标注集标签
    List<Integer> trainLabelResult = getLabelResult(trainDto);
    getTrainDataMap(trainLabelResult);
  }

  private List<Integer> getLabelResult(LabelFunctionDTO dto) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<LabelFunctionDTO> request = new HttpEntity<>(dto, headers);
    RequestConfig config = RequestConfig.custom().setSocketTimeout(1000 * 60).build();
    RequestConfigHolder.bind(config);
    ResponseEntity<List<Integer>> response;
    try {
      response =
          restTemplate.exchange(
              algorithmProperties.getLabelFunctionPath(),
              HttpMethod.POST,
              request,
              new ParameterizedTypeReference<>() {
              });
      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new BusinessIllegalStateException("调用labelFunction失败");
      }
      return response.getBody();
    } catch (Exception e) {
      throw new BusinessIllegalStateException("调用labelFunction失败", e);
    } finally {
      RequestConfigHolder.clear();
    }
  }

  private void getTestDataMap(List<Integer> testLabelResult) {
    List<TestDataPO> allByTaskId = testDataService.getAllUnShowByTaskId(ruleDescribe.getTaskId());
    getData(testLabelResult, allByTaskId);
  }

  private void getValDataMap(List<Integer> testLabelResult) {
    List<TestDataPO> allByTaskId = testDataService.getAllShowByTaskId(ruleDescribe.getTaskId());
    getData(testLabelResult, allByTaskId);
  }

  private void getData(List<Integer> testLabelResult, List<TestDataPO> allByTaskId) {
    if (allByTaskId.isEmpty()) {
      return;
    }
    if (allByTaskId.size() <= testLabelResult.size()) {
      for (int i = 0; i < allByTaskId.size(); i++) {
        testLabelMap.put(allByTaskId.get(i).getSentence(), getLabel(testLabelResult.get(i)));
      }
    } else {
      for (int i = 0; i < testLabelResult.size(); i++) {
        testLabelMap.put(allByTaskId.get(i).getSentence(), getLabel(testLabelResult.get(i)));
      }
      for (int i = testLabelResult.size(); i < allByTaskId.size(); i++) {
        testLabelMap.put(allByTaskId.get(i).getSentence(), -1);
      }
    }
  }

  private void getTrainDataMap(List<Integer> trainLabelResult) {
    Long total = unlabelDataService.countUnlabelDataByTaskId(ruleDescribe.getTaskId());
    if (total == 0) {
      return;
    }
    long totalPage = PageUtil.totalPage(total, 10000);
    if (total <= trainLabelResult.size()) {
      for (int i = 0; i < totalPage; i++) {
        List<UnlabelDataPO> unlabeledDataPOS =
            unlabelDataService.pageByTaskId(ruleDescribe.getTaskId(), i * 10000L, 10000);
        for (int j = 0; j < unlabeledDataPOS.size(); j++) {
          trainLabelMap.put(
              unlabeledDataPOS.get(j).getSentence(), getLabel(trainLabelResult.get(j + i * 10000)));
        }
      }
    } else {
      for (int i = 0; i < totalPage; i++) {
        List<UnlabelDataPO> unlabeledDataPOS =
            unlabelDataService.pageByTaskId(ruleDescribe.getTaskId(), i * 10000L, 10000);
        for (int j = 0; j < unlabeledDataPOS.size(); j++) {
          if (j + i * 10000 < trainLabelResult.size()) {
            trainLabelMap.put(
                unlabeledDataPOS.get(j).getSentence(),
                getLabel(trainLabelResult.get(j + i * 10000)));
          } else {
            trainLabelMap.put(unlabeledDataPOS.get(j).getSentence(), -1);
          }
        }
      }
    }
  }

  @Override
  public int label(String sentence, DatasetType datasetType) {
    if (datasetType == DatasetType.TEST) {
      return testLabelMap.getOrDefault(sentence, -1);
    } else if (datasetType == DatasetType.UNLABELED) {
      return trainLabelMap.getOrDefault(sentence, -1);
    } else {
      return -1;
    }
  }

  @Override
  public void destroy() {
    testLabelMap.clear();
    testLabelMap = null;
    trainLabelMap.clear();
    trainLabelMap = null;
    super.destroy();
  }

  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  public void setTestDataPath(String testDataPath) {
    this.testDataPath = testDataPath;
  }

  public void setTrainDataPath(String trainDataPath) {
    this.trainDataPath = trainDataPath;
  }

  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  public void setValDataPath(String valDataPath) {
    this.valDataPath = valDataPath;
  }
}
