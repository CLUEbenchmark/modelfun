package com.wl.xc.modelfun.tasks.file.handlers;


import static com.wl.xc.modelfun.commons.enums.DatasetType.TEST_SHOW;

import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.entities.model.HFWordInput;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.tasks.file.FileTaskHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @version 1.0
 * @date 2022/4/14 10:56
 */
@Slf4j
@Component
public class TestDataTopKeywordHandler implements FileTaskHandler {

  private DatasetDetailService datasetDetailService;

  private OssService ossService;

  private RestTemplate restTemplate;

  private AlgorithmProperties algorithmProperties;

  private LabelInfoService labelInfoService;

  @Override
  public FileTaskType getType() {
    return FileTaskType.HF_WORD;
  }

  @Override
  public void handle(FileTask fileTask) {
    log.info("[TestDataTopKeywordHandler.handle] 高频词任务开始");
    DatasetDetailPO datasetDetailPO = datasetDetailService.selectByTaskIdAndType(fileTask.getTaskId(),
        TEST_SHOW.getType());
    String path = datasetDetailPO.getFileAddress();
    String filePath = ossService.getUrlSigned(path, 30 * 60 * 1000);
    HFWordInput datasetInput = new HFWordInput();
    datasetInput.setDescription("");
    datasetInput.setDomainType("");
    datasetInput.setKeywords("");
    datasetInput.setName("");
    datasetInput.setNumClass(100L);
    datasetInput.setTaskType("");
    datasetInput.setTestLabelMatrix("");
    datasetInput.setTrainLabelMatrix("");
    datasetInput.setValPath(filePath);
    datasetInput.setTestPath("");
    datasetInput.setTrainPath("");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<HFWordInput> request = new HttpEntity<>(datasetInput, headers);
    ResponseEntity<Map<Integer, List<String>>> response;
    try {
      response = restTemplate.exchange(
          algorithmProperties.getKeywordTopPath(),
          HttpMethod.POST,
          request,
          new ParameterizedTypeReference<>() {
          });
    } catch (Exception e) {
      throw new BusinessIllegalStateException("调用高频词接口异常", e);
    }
    Map<Integer, List<String>> body = response.getBody();
    if (body == null) {
      log.warn("[TestDataTopKeywordHandler.handle] 高频词接口返回为空");
      return;
    }
    List<LabelInfoPO> infoPOS = labelInfoService.selectListByTaskId(fileTask.getTaskId());
    Map<Integer, LabelInfoPO> existLabel = infoPOS.stream()
        .collect(Collectors.toMap(LabelInfoPO::getLabelId, Function.identity()));
    if (existLabel.isEmpty()) {
      log.warn("[TestDataTopKeywordHandler.handle] 当前任务标签集为空");
      return;
    }
    List<LabelInfoPO> update = body.entrySet().stream().filter(entry -> existLabel.containsKey(entry.getKey()))
        .map(entry -> {
          LabelInfoPO po = existLabel.get(entry.getKey());
          LabelInfoPO infoPO = new LabelInfoPO();
          infoPO.setId(po.getId());
          infoPO.setHfWord(String.join(",", entry.getValue()));
          return infoPO;
        }).collect(Collectors.toList());
    labelInfoService.updateBatchById(update);
  }

  @Override
  public void afterHandle(FileTask fileTask) {

  }

  @Autowired
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
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
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }
}
