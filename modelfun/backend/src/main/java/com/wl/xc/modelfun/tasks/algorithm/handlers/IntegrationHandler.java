package com.wl.xc.modelfun.tasks.algorithm.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wl.xc.modelfun.commons.RequestConfigHolder;
import com.wl.xc.modelfun.commons.constants.FileCacheConstant;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.AlgorithmMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.IntegrateCallbackDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.DatasetInput;
import com.wl.xc.modelfun.entities.model.LabelModelInput;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * @version 1.0
 * @date 2022/4/20 11:45
 */
@Slf4j
@Component
public class IntegrationHandler extends AbstractHandler {

  protected IntegrationRecordsService integrationRecordsService;

  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.INTEGRATION;
  }

  @Override
  protected DatasetInput generateDatasetInput(AlgorithmTask task) {
    LabelModelInput labelModelInput = new LabelModelInput();
    // ???????????????????????????????????????????????????????????????
    String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(task.getTaskId());
    String unlabelMatrix =
        (String)
            stringRedisTemplate
                .opsForHash()
                .get(cacheKey, FileCacheConstant.INTEGRATE_UNLABEL_MATRIX);
    if (StringUtils.isNotBlank(unlabelMatrix)) {
      ossService.deleteFile(unlabelMatrix);
    }
    unlabelMatrix = getUnlabeledDataResult(task.getTaskId());
    stringRedisTemplate.opsForHash().put(cacheKey, FileCacheConstant.INTEGRATE_UNLABEL_MATRIX, unlabelMatrix);
    labelModelInput.setTrainLabelMatrix(
        ossService.getUrlSigned(unlabelMatrix, fileUploadProperties.getExpireTime()));
    labelModelInput.setTestLabelMatrix("");
    log.info("[IntegrationHandler] ???????????????????????????????????????{}", unlabelMatrix);
    String url =
        AlgorithmMethods.generateUrl(
            algorithmProperties.getAlgorithmCallbackUrl(), CallBackAction.INTEGRATION);
    labelModelInput.setCallback(url);
    labelModelInput.setTaskId(task.getTaskId());
    labelModelInput.setRecordId(task.getRecordId());
    return labelModelInput;
  }

  @Override
  protected DatasetDetailPO getTestDatasetFile(Long taskId) {
    // ????????????????????????????????????????????????
    return datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_SHOW.getType());
  }

  @Override
  protected void internalHandle(AlgorithmTask task, DatasetInput datasetInput) {
    send(task, datasetInput);
    // ?????????????????????websocket????????????
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.INTEGRATED_SUCCESS);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), "??????????????????", true));
    String uid = (String) task.getParams().get(SESSION_UID);
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  protected String getTestDataResult(Long taskId) {
    // ????????????????????????????????????
    // 2022???5???7???13:36:06???????????????????????????????????????
    return "";
  }

  @Override
  protected void handleOnError(AlgorithmTask task, Exception exception) {
    log.error("[IntegrationHandler.handleOnError] ??????????????????", exception);
    Long recordId = task.getRecordId();
    IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
    integrationRecordsPO.setId(recordId);
    integrationRecordsPO.setIntegrateStatus(2);
    integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
    integrationRecordsService.updateById(integrationRecordsPO);
    String msg;
    if (exception instanceof BusinessException) {
      msg = String.format("????????????????????????????????????%s", exception.getMessage());
    } else {
      msg = String.format("????????????????????????????????????%s???", "???????????????????????????????????????");
    }
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.INTEGRATED_FAIL);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), msg, false));
    String uid = (String) task.getParams().get(SESSION_UID);
    WebSocketHandler.sendByUid(uid, dto);
    // ?????????????????????????????????oss??????????????????????????????
  }

  @Override
  protected void send(AlgorithmTask task, DatasetInput datasetInput) {
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(-1).build();
    RequestConfigHolder.bind(requestConfig);

    try {
      log.info("[IntegrationHandler.send] ????????????????????????");
      ResponseEntity<String> responseEntity =
          restTemplate.postForEntity(
              algorithmProperties.getIntegratePath(), datasetInput, String.class);
      String result = responseEntity.getBody();
      if (StringUtils.isBlank(result)) {
        throw new BusinessIllegalStateException("??????????????????, ??????????????????");
      }
      log.info("[IntegrationHandler.send] ?????????????????????????????????{}", result);
    } catch (RestClientResponseException responseException) {
      throw new BusinessIllegalStateException(responseException.getMessage(), responseException);
    } finally {
      RequestConfigHolder.clear();
    }
    long now = System.currentTimeMillis();
    // ?????????????????????????????????3??????
    long timeout = now + 12 * 60 * 60 * 1000;
    String key = RedisKeyMethods.getIntegrateCacheKey(task.getTaskId(), task.getRecordId());
    while (System.currentTimeMillis() < timeout && !checkResult(key)) {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        throw new BusinessIllegalStateException("???????????????????????????", e);
      }
    }
    if (!checkResult(key)) {
      throw new BusinessIllegalStateException("?????????????????????");
    }
    // ????????????????????????
    String value = stringRedisTemplate.opsForValue().get(key);
    IntegrateCallbackDTO result;
    try {
      if (StringUtils.isBlank(value)) {
        throw new BusinessIllegalStateException("???????????????????????????");
      }
      try {
        result = objectMapper.readValue(value, IntegrateCallbackDTO.class);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("?????????????????????????????????", e);
      }
      if (!result.getState()) {
        log.error("[IntegrationHandler.send] ????????????????????????????????????{}", result.getDetail());
        throw new BusinessIllegalStateException(result.getDetail());
      }
    } finally {
      if (value != null) {
        stringRedisTemplate.delete(key);
      }
    }
    try {
      // ????????????????????????
      Long recordId = task.getRecordId();
      IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
      integrationRecordsPO.setId(recordId);
      integrationRecordsPO.setIntegrateStatus(1);
      integrationRecordsPO.setVoteModelAddress(result.getResults().getLabelModelPath());
      integrationRecordsPO.setMappingModelAddress(result.getResults().getMappingModelPath());
      integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
      // ????????????????????????
      integrationRecordsService.updateById(integrationRecordsPO);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("?????????????????????????????????????????????", e);
    }
  }

  private boolean checkResult(String key) {
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

}
