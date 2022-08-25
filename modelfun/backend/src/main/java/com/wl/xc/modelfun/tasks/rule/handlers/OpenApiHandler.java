package com.wl.xc.modelfun.tasks.rule.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.RequestConfigHolder;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.model.OpenApiReq;
import com.wl.xc.modelfun.entities.model.OpenApiResponse;
import com.wl.xc.modelfun.entities.model.OpenApiResponse.DataDTO;
import com.wl.xc.modelfun.entities.model.OpenApiRule;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * 调用外部系统接口打标记的处理器
 *
 * @version 1.0
 * @date 2022/4/15 14:48
 */
@Slf4j
public class OpenApiHandler extends AbstractOutLabelHandler {

  private final OpenApiRule openApiRule;

  private final RestTemplate restTemplate;

  private final Long taskId;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private ObjectMapper objectMapper;

  private Map<String, Integer> testMap;

  private Map<String, Integer> trainMap;

  public OpenApiHandler(Long taskId, OpenApiRule openApiRule, RestTemplate restTemplate) {
    super(taskId);
    this.openApiRule = openApiRule;
    this.restTemplate = restTemplate;
    this.taskId = taskId;
  }

  @Override
  public RuleType getRuleType() {
    return RuleType.OPEN_API;
  }

  @Override
  public void init() {
    super.init();
    initTestMap(openApiRule);
    initTrainMap(openApiRule);
  }

  private void initTestMap(OpenApiRule openApiRule) {
    List<TestDataPO> allTestDataset = testDataService.getAllByTaskId(taskId);
    Integer batchSize = openApiRule.getBatchSize();
    if (batchSize == null || batchSize <= 0) {
      batchSize = 500;
    }
    testMap = new HashMap<>(allTestDataset.size());
    int total = allTestDataset.size();
    int page = (int) PageUtil.totalPage(total, batchSize);
    for (int i = 0; i < page; i++) {
      int limit = (i < page - 1) ? batchSize : total - i * batchSize;
      ArrayList<String> list = new ArrayList<>(limit);
      for (int j = 0; j < limit; j++) {
        list.add(allTestDataset.get(i * batchSize + j).getSentence());
      }
      OpenApiReq openApiReq = new OpenApiReq();
      openApiReq.setSentences(list);
      sendAndPutMap(openApiReq, testMap);
    }
  }

  private void initTrainMap(OpenApiRule openApiRule) {
    Integer batchSize = openApiRule.getBatchSize();
    if (batchSize == null || batchSize <= 0) {
      batchSize = 500;
    }
    Long unlabeledDataCount = unlabelDataService.countUnlabelDataByTaskId(taskId);
    trainMap = new HashMap<>(unlabeledDataCount.intValue());
    long totalPage = PageUtil.totalPage(unlabeledDataCount, 10000L);
    // 每次从数据库中取10000条数据进行计算，防止内存溢出
    for (int i = 0; i < totalPage; i++) {
      long offset = i * 10000L;
      List<UnlabelDataPO> unlabelDataPOList =
          unlabelDataService.pageByTaskId(taskId, offset, 10000);
      int total = unlabelDataPOList.size();
      int page = (int) PageUtil.totalPage(total, batchSize);
      for (int l = 0; l < page; l++) {
        int limit = (l < page - 1) ? batchSize : total - l * batchSize;
        ArrayList<String> list = new ArrayList<>(limit);
        for (int j = 0; j < limit; j++) {
          list.add(unlabelDataPOList.get(l * batchSize + j).getSentence());
        }
        OpenApiReq openApiReq = new OpenApiReq();
        openApiReq.setSentences(list);
        sendAndPutMap(openApiReq, trainMap);
      }
    }
  }

  private void sendAndPutMap(OpenApiReq openApiReq, Map<String, Integer> map) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<OpenApiReq> reqHttpEntity = new HttpEntity<>(openApiReq, headers);
    log.info("[OpenApiHandler.sendAndPutMap] 发送一次外部系统请求，请求语料数量：{}", openApiReq.getSentences().size());
    ResponseEntity<String> response;
    try {
      // 设置读超时时间为5分钟
      RequestConfigHolder.bind(
          RequestConfig.custom().setSocketTimeout(1000 * 60 * 5).setConnectTimeout(10000).build());
      response = restTemplate.postForEntity(openApiRule.getHost(),
          reqHttpEntity, String.class);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("调用外部系统接口失败", e);
    } finally {
      RequestConfigHolder.clear();
    }
    String body = response.getBody();
    if (body == null) {
      throw new BusinessIllegalStateException("调用外部系统接口失败，接口返回为空");
    }
    OpenApiResponse openApiResp;
    try {
      openApiResp = objectMapper.readValue(body, OpenApiResponse.class);
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException("调用外部系统接口失败，接口返回格式错误");
    }
    if (openApiResp.getCode() != 0) {
      throw new BusinessIllegalStateException(openApiResp.getMsg(), openApiResp.getCode());
    }
    List<DataDTO> data = openApiResp.getData();
    if (data == null || data.isEmpty()) {
      log.warn("[OpenApiHandler.sendAndPutMap] 接口返回数据为空");
      return;
    }
    log.info("[OpenApiHandler.sendAndPutMap] 外部系统请求成功返回，返回标签数量：{}", data.size());
    for (DataDTO dataDTO : data) {
      map.put(dataDTO.getSentence(), getLabel(dataDTO.getLabelId()));
    }
  }

  @Override
  public int label(String sentence, DatasetType datasetType) {
    if (DatasetType.TEST == datasetType) {
      return testMap.getOrDefault(sentence, -1);
    } else if (DatasetType.UNLABELED == datasetType) {
      return trainMap.getOrDefault(sentence, -1);
    } else {
      return -1;
    }
  }

  @Override
  public void destroy() {
    if (testMap != null) {
      testMap.clear();
    }
    if (trainMap != null) {
      trainMap.clear();
    }
    super.destroy();
  }

  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
