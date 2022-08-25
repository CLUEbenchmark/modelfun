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
    // 从缓存中先查询老的文件，如果有的话，先删除
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
    log.info("[IntegrationHandler] 未标注集标签矩阵文件地址：{}", unlabelMatrix);
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
    // 规则集成任务需要显示的测试数据集
    return datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_SHOW.getType());
  }

  @Override
  protected void internalHandle(AlgorithmTask task, DatasetInput datasetInput) {
    send(task, datasetInput);
    // 集成成功。通过websocket通知前端
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.INTEGRATED_SUCCESS);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), "规则集成成功", true));
    String uid = (String) task.getParams().get(SESSION_UID);
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  protected String getTestDataResult(Long taskId) {
    // 总的显示的测试集语料数量
    // 2022年5月7日13:36:06修改，不需要测试集标注结果
    return "";
  }

  @Override
  protected void handleOnError(AlgorithmTask task, Exception exception) {
    log.error("[IntegrationHandler.handleOnError] 规则集成失败", exception);
    Long recordId = task.getRecordId();
    IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
    integrationRecordsPO.setId(recordId);
    integrationRecordsPO.setIntegrateStatus(2);
    integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
    integrationRecordsService.updateById(integrationRecordsPO);
    String msg;
    if (exception instanceof BusinessException) {
      msg = String.format("规则集成失败，错误信息：%s", exception.getMessage());
    } else {
      msg = String.format("规则集成失败，错误信息：%s！", "系统内部错误，请联系管理员");
    }
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.INTEGRATED_FAIL);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), msg, false));
    String uid = (String) task.getParams().get(SESSION_UID);
    WebSocketHandler.sendByUid(uid, dto);
    // 发生错误时，删除上传的oss文件？（是否有需要）
  }

  @Override
  protected void send(AlgorithmTask task, DatasetInput datasetInput) {
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(-1).build();
    RequestConfigHolder.bind(requestConfig);

    try {
      log.info("[IntegrationHandler.send] 发送规则集成请求");
      ResponseEntity<String> responseEntity =
          restTemplate.postForEntity(
              algorithmProperties.getIntegratePath(), datasetInput, String.class);
      String result = responseEntity.getBody();
      if (StringUtils.isBlank(result)) {
        throw new BusinessIllegalStateException("规则集成失败, 返回结果为空");
      }
      log.info("[IntegrationHandler.send] 规则集成请求返回结果：{}", result);
    } catch (RestClientResponseException responseException) {
      throw new BusinessIllegalStateException(responseException.getMessage(), responseException);
    } finally {
      RequestConfigHolder.clear();
    }
    long now = System.currentTimeMillis();
    // 规则集成默认超时时间为3分钟
    long timeout = now + 12 * 60 * 60 * 1000;
    String key = RedisKeyMethods.getIntegrateCacheKey(task.getTaskId(), task.getRecordId());
    while (System.currentTimeMillis() < timeout && !checkResult(key)) {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        throw new BusinessIllegalStateException("集成任务线程中断！", e);
      }
    }
    if (!checkResult(key)) {
      throw new BusinessIllegalStateException("集成任务超时！");
    }
    // 更新规则集成状态
    String value = stringRedisTemplate.opsForValue().get(key);
    IntegrateCallbackDTO result;
    try {
      if (StringUtils.isBlank(value)) {
        throw new BusinessIllegalStateException("集成结果返回为空！");
      }
      try {
        result = objectMapper.readValue(value, IntegrateCallbackDTO.class);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("规则集成结果解析失败！", e);
      }
      if (!result.getState()) {
        log.error("[IntegrationHandler.send] 规则集成失败，错误信息：{}", result.getDetail());
        throw new BusinessIllegalStateException(result.getDetail());
      }
    } finally {
      if (value != null) {
        stringRedisTemplate.delete(key);
      }
    }
    try {
      // 更新规则集成记录
      Long recordId = task.getRecordId();
      IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
      integrationRecordsPO.setId(recordId);
      integrationRecordsPO.setIntegrateStatus(1);
      integrationRecordsPO.setVoteModelAddress(result.getResults().getLabelModelPath());
      integrationRecordsPO.setMappingModelAddress(result.getResults().getMappingModelPath());
      integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
      // 更新规则集成记录
      integrationRecordsService.updateById(integrationRecordsPO);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("服务器内部错误，请联系管理员！", e);
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
