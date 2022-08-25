package com.wl.xc.modelfun.tasks.algorithm.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.methods.AlgorithmMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.entities.dto.CallbackEventDTO;
import com.wl.xc.modelfun.entities.dto.NerOneClickDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmHandler;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.daemon.OneClickCallbackListener;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * @version 1.0
 * @date 2022/5/26 17:51
 */
@Slf4j
@Component
public class NerOneClickHandler implements AlgorithmHandler {

  private StringRedisTemplate stringRedisTemplate;

  private RestTemplate restTemplate;

  private AlgorithmProperties algorithmProperties;

  private LabelInfoService labelInfoService;

  private DatasetDetailService datasetDetailService;

  private IntegrationRecordsService integrationRecordsService;

  private OssService ossService;

  private OneClickCallbackListener oneClickCallbackListener;

  private UnlabelDataService unlabelDataService;

  @Override
  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.NER_ONE_CLICK;
  }

  @Override
  public void handle(AlgorithmTask task) {
    log.info("[NerOneClickHandler.handle] 开始一键标注");
    try {
      String uid = (String) task.getParams().get(SESSION_UID);
      String cacheKey = RedisKeyMethods.getOneClickCacheKey(task.getTaskId());
      List<LabelInfoPO> infoPOS = labelInfoService.selectListByTaskId(task.getTaskId());
      List<String> schemas = infoPOS.stream().map(LabelInfoPO::getLabelDesc).collect(Collectors.toList());
      NerOneClickDTO dto = new NerOneClickDTO();
      String url =
          AlgorithmMethods.generateUrl(
              algorithmProperties.getAlgorithmCallbackUrl(), CallBackAction.NER_ONE_CLICK);
      dto.setCallback(url);
      dto.setTaskId(task.getTaskId());
      dto.setRecordId(task.getRecordId());
      dto.setModelName("uie");
      DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(task.getTaskId(),
          DatasetType.TEST.getType());
      dto.setTestPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000));
      detailPO = datasetDetailService.selectByTaskIdAndType(task.getTaskId(), DatasetType.UNLABELED.getType());
      dto.setUnlabeledPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000));
      detailPO = datasetDetailService.selectByTaskIdAndType(task.getTaskId(), DatasetType.NER_TUNE.getType());
      dto.setTunePath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000));
      dto.setSchemas(schemas);
      log.info("[NerOneClickHandler.handle] 发送一键标注请求，地址：{}", algorithmProperties.getNerOneClickUrl());
      log.info("[NerOneClickHandler.handle] 发送一键标注请求，参数：{}", dto);
      String result = restTemplate.postForObject(algorithmProperties.getNerOneClickUrl(), dto, String.class);
      log.info("[NerOneClickHandler.handle] 接收到一键标注返回结果，结果：{}", result);
      CallbackEventDTO eventDTO = new CallbackEventDTO();
      // 超时时间为6分钟
      eventDTO.setExecuteTime(System.currentTimeMillis() + 600 * 1000);
      log.info("[NerOneClickHandler.handle] 超时时间 {}", new Date(eventDTO.getExecuteTime()));
      eventDTO.setTopic("ner_one_click");
      eventDTO.setTaskId(task.getTaskId());
      eventDTO.setRecordId(task.getRecordId());
      oneClickCallbackListener.addCallbackListener(eventDTO);
    } catch (Exception e) {
      handleError(task, e);
    }
  }

  private void handleError(AlgorithmTask task, Exception e) {
    log.error("[NerOneClickHandler.handleError]", e);
    String msg = "服务器内部错误，请联系管理员";
    if (e instanceof RestClientResponseException) {
      RestClientResponseException exception = (RestClientResponseException) e;
      msg = exception.getResponseBodyAsString();
    }
    IntegrationRecordsPO po = new IntegrationRecordsPO();
    po.setId(task.getRecordId());
    po.setLabeled(2);
    po.setIntegrateStatus(2);
    integrationRecordsService.updateById(po);
    String uid = (String) task.getParams().get(SESSION_UID);
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.ONE_CLICK_FAIL);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), "", msg, false));
    WebSocketHandler.sendByUid(uid, dto);
    String cacheKey = RedisKeyMethods.getOneClickCacheKey(task.getTaskId());
    stringRedisTemplate.delete(cacheKey);
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
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

  @Autowired
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setOneClickCallbackListener(OneClickCallbackListener oneClickCallbackListener) {
    this.oneClickCallbackListener = oneClickCallbackListener;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }
}
