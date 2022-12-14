package com.wl.xc.modelfun.tasks.algorithm.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.UPDATE_TIME;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskTrainKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wl.xc.modelfun.commons.RequestConfigHolder;
import com.wl.xc.modelfun.commons.constants.CommonConstant;
import com.wl.xc.modelfun.commons.constants.FileCacheConstant;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.ModelType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.AlgorithmMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.TrainCallbackDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.DatasetInput;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.model.TrainCallbackEvent;
import com.wl.xc.modelfun.entities.model.TrainInput;
import com.wl.xc.modelfun.entities.po.AutoLabelResultPO;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.SimpleAutoLabelResult;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.TrainRecordsPO;
import com.wl.xc.modelfun.service.IntegrateLabelResultService;
import com.wl.xc.modelfun.service.ModelTrainService;
import com.wl.xc.modelfun.service.TrainRecordsService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.daemon.TrainResultCallbackListener;
import com.wl.xc.modelfun.tasks.file.handlers.text.TextLabelDataModel;
import com.wl.xc.modelfun.utils.PageUtil;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * @version 1.0
 * @date 2022/4/20 11:45
 */
@Slf4j
@Component
public class ModelTrainHandler extends AbstractHandler {

  private ModelTrainService modelTrainService;

  private TrainRecordsService trainRecordsService;

  private IntegrateLabelResultService integrateLabelResultService;

  private TrainResultCallbackListener trainResultCallbackListener;

  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.MODEL_TRAIN;
  }

  @Override
  protected DatasetInput generateDatasetInput(AlgorithmTask task) {
    TrainInput trainInput = new TrainInput();
    Long taskId = task.getTaskId();
    trainInput.setTaskId(taskId);
    String url =
        AlgorithmMethods.generateUrl(
            algorithmProperties.getAlgorithmCallbackUrl(), CallBackAction.TEXT_MODEL_TRAIN);
    trainInput.setCallback(url);
    trainInput.setRecordId(task.getRecordId());
    String autoLabelFile = generateAutoLabelFile(taskId);
    TrainRecordsPO recordsPO = new TrainRecordsPO();
    recordsPO.setId(task.getRecordId());
    recordsPO.setTrainFile(autoLabelFile);
    List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(taskId);
    List<String> collect = labelInfoPOS.stream().map(LabelInfoPO::getLabelDesc).collect(Collectors.toList());
    try {
      recordsPO.setLabelArray(objectMapper.writeValueAsString(collect));
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException("?????????????????????", e);
    }
    trainRecordsService.updateById(recordsPO);
    DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_SHOW.getType());
    if (detailPO == null) {
      throw new BusinessIllegalStateException("??????id???" + taskId + "?????????????????????????????????");
    }
    // ???????????????????????????
    trainInput.setValPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000));
    // ???????????????????????????
    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
    String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(task.getTaskId());
    Path path = prepareFile(taskId);
    String upload = writeAndUpload(taskId, path, this::writeTrainLabelFile);
    stringRedisTemplate.opsForHash().put(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT, upload);
    // ????????????????????????????????????
    trainInput.setTrainLabel(ossService.getUrlSigned(upload, fileUploadProperties.getExpireTime()));
    // ????????????????????????
    LocalDateTime updateTime = (LocalDateTime) task.getParams().get(UPDATE_TIME);
    String labelPath = getLabelPath(taskId, updateTime);
    if (labelPath != null) {
      trainInput.setLabeledPath(ossService.getUrlSigned(labelPath, fileUploadProperties.getExpireTime()));
    }
    trainInput.setLabelModelPrediction(ossService.getUrlSigned(getTestDataResult(taskId), 3600 * 1000 * 12));
    return trainInput;
  }

  /**
   * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
   *
   * @param taskId ??????ID
   * @return ???????????????oss??????
   */
  @Override
  protected String getTrainPath(Long taskId) {
    Path path = prepareFile(taskId);
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        long total = integrateLabelResultService.countCorrectByTaskId(taskId);
        long totalPage = PageUtil.totalPage(total, 10000L);
        for (long i = 0; i < totalPage; i++) {
          List<SimpleAutoLabelResult> autoList = integrateLabelResultService.pageCorrectByTaskId(taskId, i * 10000L,
              10000);
          for (SimpleAutoLabelResult resultPO : autoList) {
            writer.write(objectMapper.writeValueAsString(resultPO));
            writer.newLine();
          }
        }
      } catch (IOException e) {
        throw new BusinessIOException("??????????????????????????????", e);
      }
      File file = path.toFile();
      FileUpload upload = new FileUpload();
      upload.setFile(file);
      upload.setDestPath(fileUploadProperties.getOssTempPath() + "/" + file.getName());
      upload.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      Map<String, String> tag = new HashMap<>();
      tag.put(CommonConstant.OSS_TEMP_TAG, "30");
      upload.setTagMap(tag);
      ossService.uploadFiles(Collections.singletonList(upload));
      String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(taskId);
      stringRedisTemplate.opsForHash().put(cacheKey, FileCacheConstant.TRAIN_PATH, upload.getDestPath());
      log.info("[ModelTrainHandler.getTrainPath] ????????????????????????OSS?????????name={}", upload.getDestPath());
      return upload.getDestPath();
    } finally {
      if (!path.toFile().delete()) {
        log.warn("[ModelTrainHandler.getTrainPath] ???????????????????????????path={}", path);
      }
    }
  }

  /**
   * ?????????????????????????????????????????????????????????
   *
   * @param taskId ??????ID
   * @return ?????????????????????
   */
  private String getLabelPath(Long taskId, LocalDateTime createTime) {
    DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
    if (detailPO == null) {
      return null;
    }
    if (createTime != null && detailPO.getUpdateDatetime().isBefore(createTime)) {
      return detailPO.getFileAddress();
    }
    Path path = prepareFile(taskId);
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        // ?????????????????????????????????
        Integer trainType = DatasetType.TRAIN.getType();
        long total = testDataService.countByTaskIdAndType(taskId, trainType);
        long totalPage = PageUtil.totalPage(total, 10000L);
        for (long i = 0; i < totalPage; i++) {
          List<TestDataPO> autoList =
              testDataService.pageTestData(taskId, trainType, i * 10000L, 10000);
          for (TestDataPO resultPO : autoList) {
            TextLabelDataModel model = new TextLabelDataModel();
            model.setId(resultPO.getDataId());
            model.setSentence(resultPO.getSentence());
            model.setLabel(resultPO.getLabel());
            model.setLabelDes(resultPO.getLabelDes());
            writer.write(objectMapper.writeValueAsString(model));
            writer.newLine();
          }
        }
        log.info("[ModelTrainHandler.getLabelPath] ????????????????????????????????????taskId={}", taskId);
      } catch (IOException e) {
        throw new BusinessIOException("????????????????????????????????????", e);
      }
      File file = path.toFile();
      FileUpload upload = new FileUpload();
      upload.setFile(file);
      upload.setDestPath(detailPO.getFileAddress());
      upload.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      ossService.uploadFiles(Collections.singletonList(upload));
      log.info("[ModelTrainHandler.getLabelPath] ???????????????OSS?????????name={}", upload.getDestPath());
      DatasetDetailPO detailPO1 = new DatasetDetailPO();
      detailPO1.setUpdateDatetime(LocalDateTime.now());
      detailPO1.setId(detailPO.getId());
      datasetDetailService.updateById(detailPO1);
      return upload.getDestPath();
    } finally {
      if (!path.toFile().delete()) {
        log.warn("[ModelTrainHandler.getLabelPath] ???????????????????????????path={}", path);
      }
    }
  }

  /**
   * ??????????????????????????????????????????????????????????????????????????????ID??????
   *
   * @param writer ???????????????
   * @param taskId ??????ID
   */
  private void writeTrainLabelFile(Writer writer, Long taskId) {
    try {
      long total = integrateLabelResultService.countCorrectByTaskId(taskId);
      long totalPage = PageUtil.totalPage(total, 10000L);
      for (long i = 0; i < totalPage; i++) {
        List<AutoLabelResultPO> autoList = integrateLabelResultService.pageLabelCorrectByTaskId(taskId, i * 10000L,
            10000);
        String collect = autoList.stream().map(po -> po.getLabel().toString()).collect(Collectors.joining(","));
        writer.write(collect);
        if (i != totalPage - 1) {
          writer.write(",");
        }
      }
    } catch (IOException e) {
      throw new BusinessIOException("??????????????????????????????????????????", e);
    }
  }

  private String generateAutoLabelFile(Long taskId) {
    Path path = prepareFile(taskId);
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        long total = integrateLabelResultService.countCorrectByTaskId(taskId);
        long totalPage = PageUtil.totalPage(total, 10000L);
        for (long i = 0; i < totalPage; i++) {
          List<SimpleAutoLabelResult> autoList = integrateLabelResultService.pageCorrectByTaskId(taskId, i * 10000L,
              10000);
          for (SimpleAutoLabelResult resultPO : autoList) {
            resultPO.setType("???????????????");
            writer.write(objectMapper.writeValueAsString(resultPO));
            writer.newLine();
          }
        }
        // ?????????????????????????????????
        Integer trainType = DatasetType.TRAIN.getType();
        total = testDataService.countByTaskIdAndType(taskId, trainType);
        totalPage = PageUtil.totalPage(total, 10000L);
        for (long i = 0; i < totalPage; i++) {
          List<TestDataPO> autoList =
              testDataService.pageTestData(taskId, trainType, i * 10000L, 10000);
          for (TestDataPO resultPO : autoList) {
            SimpleAutoLabelResult simple = new SimpleAutoLabelResult();
            simple.setSentence(resultPO.getSentence());
            simple.setLabel(resultPO.getLabel().toString());
            simple.setLabelDes(resultPO.getLabelDes());
            simple.setType("???????????????");
            writer.write(objectMapper.writeValueAsString(simple));
            writer.newLine();
          }
        }
        log.info("[ModelTrainHandler.generateAutoLabelFile] ???????????????????????????????????????taskId={}", taskId);
      } catch (IOException e) {
        throw new BusinessIOException("???????????????????????????????????????", e);
      }
      File file = path.toFile();
      FileUpload upload = new FileUpload();
      upload.setFile(file);
      String parent = fileUploadProperties.getOssPrefix() + taskId + "/vote/";
      upload.setDestPath(parent + file.getName());
      upload.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      ossService.uploadFiles(Collections.singletonList(upload));
      log.info("[ModelTrainHandler.generateAutoLabelFile] ???????????????OSS?????????name={}", upload.getDestPath());
      return upload.getDestPath();
    } finally {
      try {
        Files.delete(path);
      } catch (IOException e) {
        log.error("[ModelTrainHandler.generateAutoLabelFile] ??????????????????", e);
      }
    }
  }

  @Override
  protected void internalHandle(AlgorithmTask task, DatasetInput datasetInput) {
    log.info("??????????????????");
    send(task, datasetInput);
    log.info("??????????????????");
  }

  @Override
  protected void handleOnError(AlgorithmTask task, Exception exception) {
    log.error("[ModelTrainHandler.handleOnError] ??????????????????", exception);
    // ????????????????????????
    TrainCallbackDTO trainCallbackDTO = new TrainCallbackDTO();
    trainCallbackDTO.setTaskId(task.getTaskId());
    trainCallbackDTO.setRecordId(task.getRecordId());
    modelTrainService.saveFailedTrainResult(trainCallbackDTO);
    // websocket??????
    String msg;
    if (exception instanceof BusinessException) {
      msg = String.format("????????????????????????????????????%s", exception.getMessage());
    } else {
      msg = String.format("????????????????????????????????????%s???", "???????????????????????????????????????");
    }
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.TRAIN_FAIL);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), msg, false));
    String uid = (String) task.getParams().get(SESSION_UID);
    WebSocketHandler.sendByUid(uid, dto);
    clearResource(task.getTaskId(), task.getRecordId());
  }

  @Override
  protected void send(AlgorithmTask task, DatasetInput datasetInput) {
    // ??????????????????
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000 * 60).build();
    RequestConfigHolder.bind(requestConfig);
    try {
      Integer model = (Integer) task.getParams().get("model");
      if (model == null) {
        model = ModelType.LR.getType();
      }
      long timeOut;
      if (model == ModelType.LR.getType()) {
        // LR ????????????????????????????????????????????????
        // 2022???5???23???14:27:31???LR?????????????????????????????????????????????
        timeOut = 2 * 60 * 1000 * 2;
      } else {
        timeOut = 30 * 60 * 1000 * 2;
      }
      String uid = (String) task.getParams().get(SESSION_UID);
      stringRedisTemplate.opsForValue()
          .set(getTaskTrainKey(task.getTaskId(), task.getRecordId()), uid, timeOut + 30 * 1000, TimeUnit.MILLISECONDS);
      ModelType type = ModelType.getFromType(model);
      String path = algorithmProperties.getTrainPath() + "?model=" + type.getPath();
      log.info("[ModelTrainHandler.send] ??????????????????????????????????????????{}??????????????????{}", type, path);
      log.info("[ModelTrainHandler.send] ???????????????????????????{}", datasetInput);
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(path, datasetInput, String.class);
      log.info("???????????????{}", responseEntity.getBody());
      // ??????????????????????????????
      TrainCallbackEvent event = new TrainCallbackEvent();
      event.setRecordId(task.getRecordId());
      event.setDelayMillisecond(timeOut);
      event.setTaskId(task.getTaskId());
      trainResultCallbackListener.addCallbackListener(event);
    } catch (RestClientResponseException responseException) {
      throw new BusinessIllegalStateException(responseException.getMessage(), responseException);
    } finally {
      RequestConfigHolder.clear();
    }
  }

  private void clearResource(Long taskId, Long recordId) {
    String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(taskId);
    String autoResult = (String) stringRedisTemplate.opsForHash()
        .get(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT);
    if (StringUtils.isNotBlank(autoResult)) {
      ossService.deleteFile(autoResult);
      stringRedisTemplate.opsForHash().delete(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT);
    }
    stringRedisTemplate.delete(getTaskTrainKey(taskId, recordId));
  }

  @Autowired
  public void setIntegrateLabelResultService(IntegrateLabelResultService integrateLabelResultService) {
    this.integrateLabelResultService = integrateLabelResultService;
  }

  @Autowired
  public void setTrainResultCallbackListener(
      TrainResultCallbackListener trainResultCallbackListener) {
    this.trainResultCallbackListener = trainResultCallbackListener;
  }

  @Autowired
  public void setModelTrainService(ModelTrainService modelTrainService) {
    this.modelTrainService = modelTrainService;
  }

  @Autowired
  public void setTrainRecordsService(TrainRecordsService trainRecordsService) {
    this.trainRecordsService = trainRecordsService;
  }
}
