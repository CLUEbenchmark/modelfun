package com.wl.xc.modelfun.tasks.algorithm.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.UPDATE_TIME;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickCacheKey;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickErrorKey;

import cn.hutool.core.lang.id.NanoId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.AlgorithmMethods;
import com.wl.xc.modelfun.commons.methods.FileMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.dto.AsyncRspDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.FewShotInput;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmHandler;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.file.handlers.text.TextLabelDataModel;
import com.wl.xc.modelfun.utils.PageUtil;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * @version 1.0
 * @date 2022/4/20 11:45
 */
@Slf4j
@Component
public class FewShotHandler implements AlgorithmHandler {

  private RestTemplate restTemplate;

  private FileUploadProperties fileUploadProperties;

  private AlgorithmProperties algorithmProperties;

  private TaskInfoService taskInfoService;

  private LabelInfoService labelInfoService;

  private TestDataService testDataService;

  private UnlabelDataService unlabelDataService;

  private DatasetDetailService datasetDetailService;

  private ObjectMapper objectMapper;

  private OssService ossService;

  private StringRedisTemplate stringRedisTemplate;

  protected IntegrationRecordsService integrationRecordsService;

  private final ScheduledThreadPoolExecutor executor =
      new ScheduledThreadPoolExecutor(10, new WorkThreadFactory("fewShot"));

  public FewShotHandler() {
    executor.setKeepAliveTime(60, TimeUnit.SECONDS);
    executor.setMaximumPoolSize(20);
    executor.allowCoreThreadTimeOut(true);
  }

  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.FEW_SHOT;
  }

  @Override
  public void handle(AlgorithmTask task) {
    try {
      log.info("[FewShotHandler.handle] 开始执行算法任务，任务类型：{}", getType().getName());
      Long taskId = task.getTaskId();
      TaskInfoPO taskInfoPO = taskInfoService.getById(taskId);
      if (taskInfoPO == null) {
        throw new BusinessIllegalStateException("任务id为" + taskId + "的任务不存在");
      }
      long labelCount = labelInfoService.countLabelInfoByTaskId(taskId);
      FewShotInput fewShotInput = new FewShotInput();
      fewShotInput.setNumClass(labelCount);
      fewShotInput.setTaskId(taskId);
      fewShotInput.setRecordId(task.getRecordId());
      String url =
          AlgorithmMethods.generateUrl(
              algorithmProperties.getAlgorithmCallbackUrl(), CallBackAction.TEXT_FEW_SHOT);
      fewShotInput.setCallback(url);
      LocalDateTime updateTime = (LocalDateTime) task.getParams().get(UPDATE_TIME);
      // 训练集地址
      fewShotInput.setTrainPath(
          ossService.getUrlSigned(
              rewriteTrainFile(taskId, updateTime), fileUploadProperties.getExpireTime()));
      // 未标注集地址
      fewShotInput.setUnlabeledPath(
          ossService.getUrlSigned(
              rewriteUnlabelFile(taskId, updateTime), fileUploadProperties.getExpireTime()));
      DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId,
          DatasetType.TEST_UN_SHOW.getType());
      fewShotInput.setTestPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000 * 12));
      detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_SHOW.getType());
      fewShotInput.setValPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000 * 12));
      internalHandle(task, fewShotInput);
    } catch (Exception e) {
      handleOnError(task, e);
    }
  }

  protected Path prepareFile(Long taskId) {
    String localTemp = fileUploadProperties.getTempPath();
    String fileName = NanoId.randomNanoId() + "_" + taskId + ".json";
    return FileMethods.prepareFile(localTemp, fileName);
  }

  private String rewriteTrainFile(Long taskId, LocalDateTime createTime) {
    DatasetDetailPO detailPO =
        datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
    // 如果训练集的更新时间早于上一次的集成时间，即自从上一次集成之后，训练集没有更新，则不需要重新生成训练集
    if (createTime != null && detailPO.getUpdateDatetime().isBefore(createTime)) {
      return detailPO.getFileAddress();
    }
    Path path = prepareFile(taskId);
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        long count = testDataService.countByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
        long totalPage = PageUtil.totalPage(count, 5000);
        for (int i = 0; i < totalPage; i++) {
          long offset = i * 5000L;
          List<TestDataPO> dataPOS =
              testDataService.pageTestData(taskId, DatasetType.TRAIN.getType(), offset, 5000);
          for (int j = 0; j < dataPOS.size(); j++) {
            TestDataPO testDataPO = dataPOS.get(j);
            TextLabelDataModel model = new TextLabelDataModel();
            model.setId(testDataPO.getDataId());
            model.setSentence(testDataPO.getSentence());
            model.setLabel(testDataPO.getLabel());
            model.setLabelDes(testDataPO.getLabelDes());
            writer.write(objectMapper.writeValueAsString(model));
            if (j + offset < count - 1) {
              writer.newLine();
            }
          }
        }
      } catch (IOException e) {
        throw new BusinessIllegalStateException("生成训练集失败");
      }
      FileUpload fileUpload = new FileUpload();
      fileUpload.setFile(path.toFile());
      fileUpload.setDestPath(detailPO.getFileAddress());
      ossService.uploadFiles(Collections.singletonList(fileUpload));
      DatasetDetailPO detailPO1 = new DatasetDetailPO();
      detailPO1.setUpdateDatetime(LocalDateTime.now());
      detailPO1.setId(detailPO.getId());
      datasetDetailService.updateById(detailPO1);
      return detailPO.getFileAddress();
    } finally {
      try {
        Files.delete(path);
      } catch (IOException e) {
        log.error("[FewShotHandler.rewriteTrainFile] 删除文件失败");
      }
    }
  }

  private String rewriteUnlabelFile(Long taskId, LocalDateTime createTime) {
    DatasetDetailPO detailPO =
        datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.UNLABELED.getType());
    if (createTime != null && detailPO.getUpdateDatetime().isBefore(createTime)) {
      return detailPO.getFileAddress();
    }
    Path path = prepareFile(taskId);
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        long count = unlabelDataService.countUnlabelDataByTaskId(taskId);
        long totalPage = PageUtil.totalPage(count, 5000);
        for (int i = 0; i < totalPage; i++) {
          long offset = i * 5000L;
          List<UnlabelDataPO> dataPOS = unlabelDataService.pageByTaskId(taskId, offset, 5000);
          for (int j = 0; j < dataPOS.size(); j++) {
            UnlabelDataPO unlabelDataPO = dataPOS.get(j);
            TextLabelDataModel model = new TextLabelDataModel();
            model.setId(unlabelDataPO.getDataId());
            model.setSentence(unlabelDataPO.getSentence());
            writer.write(objectMapper.writeValueAsString(model));
            if (j + offset < count - 1) {
              writer.newLine();
            }
          }
        }
      } catch (IOException e) {
        throw new BusinessIllegalStateException("生成训练集失败");
      }
      FileUpload fileUpload = new FileUpload();
      fileUpload.setFile(path.toFile());
      fileUpload.setDestPath(detailPO.getFileAddress());
      ossService.uploadFiles(Collections.singletonList(fileUpload));
      DatasetDetailPO detailPO1 = new DatasetDetailPO();
      detailPO1.setId(detailPO.getId());
      detailPO1.setUpdateDatetime(LocalDateTime.now());
      datasetDetailService.updateById(detailPO1);
      return detailPO.getFileAddress();
    } finally {
      try {
        Files.delete(path);
      } catch (IOException e) {
        log.error("[FewShotHandler.rewriteUnlabelFile] 删除文件失败");
      }
    }
  }

  protected void internalHandle(AlgorithmTask task, FewShotInput datasetInput) {
    send(task, datasetInput);
  }

  protected void handleOnError(AlgorithmTask task, Exception exception) {
    log.error("[FewShotHandler.handleOnError] 规则集成失败", exception);
    Long recordId = task.getRecordId();
    IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
    integrationRecordsPO.setId(recordId);
    integrationRecordsPO.setIntegrateStatus(2);
    integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
    integrationRecordsService.updateById(integrationRecordsPO);
    String msg;
    if (exception instanceof BusinessException) {
      msg = String.format("规则集成失败，小样本学习失败，错误信息：%s", exception.getMessage());
    } else {
      msg = String.format("规则集成失败，小样本学习失败，错误信息：%s！", "系统内部错误，请联系管理员");
    }
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), msg, false));
    dto.setEvent(WsEventType.INTEGRATED_FAIL);
    String uid = (String) task.getParams().get(SESSION_UID);
    WebSocketHandler.sendByUid(uid, dto);
    // 发生错误时，删除上传的oss文件？（是否有需要）
  }

  protected void send(AlgorithmTask task, FewShotInput datasetInput) {
    long timeout;
    try {
      log.info("[FewShotHandler.send] 发送小样本学习请求,请求体为：{}", datasetInput);
      String url = algorithmProperties.getFewShotUrl() + "?model=ptunning";
      ResponseEntity<AsyncRspDTO> responseEntity =
          restTemplate.postForEntity(url, datasetInput, AsyncRspDTO.class);
      AsyncRspDTO result = responseEntity.getBody();
      if (result == null) {
        throw new BusinessIllegalStateException("小样本学习失败");
      }
      log.info(
          "[FewShotHandler.send] 小样本学习请求返回结果：msg:{}，timeout：{}",
          result.getMessage(),
          result.getTimeout());
      timeout = result.getTimeout();
    } catch (RestClientResponseException responseException) {
      throw new BusinessIllegalStateException(responseException.getMessage(), responseException);
    }
    // 小样本学习默认超时时间为半小时
    timeout = timeout == 0 ? 1800 : timeout;
    String key = RedisKeyMethods.getFewShowKey(task.getTaskId(), task.getRecordId());
    // 设置redis的缓存
    String uid = (String) task.getParams().get(SESSION_UID);
    stringRedisTemplate.opsForValue().set(key, uid, timeout + 30, TimeUnit.SECONDS);
    executor.schedule(
        () -> timeOutListener(task.getTaskId(), task.getRecordId(), uid),
        timeout,
        TimeUnit.SECONDS);
  }

  private void timeOutListener(Long taskId, Long recordId, String uid) {
    String key = RedisKeyMethods.getFewShowKey(taskId, recordId);
    Boolean hasKey = stringRedisTemplate.hasKey(key);
    if (!Boolean.TRUE.equals(hasKey)) {
      return;
    }
    log.info("[FewShotHandler.timeOutListener] 小样本学习超时");
    String textClickCacheKey = getTextClickCacheKey(taskId, recordId);
    hasKey = stringRedisTemplate.hasKey(textClickCacheKey);
    if (Boolean.TRUE.equals(hasKey)) {
      String errorKey = getTextClickErrorKey(taskId, recordId);
      stringRedisTemplate.opsForValue().set(errorKey, "小样本学习超时", 300, TimeUnit.SECONDS);
    } else {
      IntegrationRecordsPO recordsPO = new IntegrationRecordsPO();
      recordsPO.setId(recordId);
      recordsPO.setIntegrateStatus(2);
      recordsPO.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsService.updateById(recordsPO);
      TaskInfoPO po = taskInfoService.getById(taskId);
      WebsocketDTO dto = new WebsocketDTO();
      dto.setEvent(WsEventType.INTEGRATED_FAIL);
      dto.setData(WebsocketDataDTO.create(taskId, po.getName(), "规则集成超时：小样本学习超时", false));
      WebSocketHandler.sendByUid(uid, dto);
      stringRedisTemplate.delete(key);
    }
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void setAlgorithmProperties(AlgorithmProperties algorithmProperties) {
    this.algorithmProperties = algorithmProperties;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
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
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
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
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }
}
