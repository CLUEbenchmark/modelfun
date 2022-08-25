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
      throw new BusinessIllegalStateException("服务器内部错误", e);
    }
    trainRecordsService.updateById(recordsPO);
    DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_SHOW.getType());
    if (detailPO == null) {
      throw new BusinessIllegalStateException("任务id为" + taskId + "的可见测试集文件不存在");
    }
    // 可见测试集文件地址
    trainInput.setValPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000));
    // 获取自动标注的结果
    // 这里不直接使用上一步自动标注返回的文件，因为后期可能会对标注结果进行删减
    String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(task.getTaskId());
    Path path = prepareFile(taskId);
    String upload = writeAndUpload(taskId, path, this::writeTrainLabelFile);
    stringRedisTemplate.opsForHash().put(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT, upload);
    // 高置信数据对应的标签结果
    trainInput.setTrainLabel(ossService.getUrlSigned(upload, fileUploadProperties.getExpireTime()));
    // 训练集对应的文件
    LocalDateTime updateTime = (LocalDateTime) task.getParams().get(UPDATE_TIME);
    String labelPath = getLabelPath(taskId, updateTime);
    if (labelPath != null) {
      trainInput.setLabeledPath(ossService.getUrlSigned(labelPath, fileUploadProperties.getExpireTime()));
    }
    trainInput.setLabelModelPrediction(ossService.getUrlSigned(getTestDataResult(taskId), 3600 * 1000 * 12));
    return trainInput;
  }

  /**
   * 生成高置信文件的地址，因为模型训练只需要高置信部分的数据，所以文件需要重新生成
   *
   * @param taskId 任务ID
   * @return 高置信文件oss地址
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
        throw new BusinessIOException("生成高置信文件错误！", e);
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
      log.info("[ModelTrainHandler.getTrainPath] 上传高置信文件到OSS成功，name={}", upload.getDestPath());
      return upload.getDestPath();
    } finally {
      if (!path.toFile().delete()) {
        log.warn("[ModelTrainHandler.getTrainPath] 删除临时文件失败，path={}", path);
      }
    }
  }

  /**
   * 生成训练集文件，生成之后更新数据集信息
   *
   * @param taskId 任务ID
   * @return 训练集文件地址
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
        // 根据训练集数据生成文件
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
        log.info("[ModelTrainHandler.getLabelPath] 生成新的训练集文件成功，taskId={}", taskId);
      } catch (IOException e) {
        throw new BusinessIOException("生成新的训练集文件错误！", e);
      }
      File file = path.toFile();
      FileUpload upload = new FileUpload();
      upload.setFile(file);
      upload.setDestPath(detailPO.getFileAddress());
      upload.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      ossService.uploadFiles(Collections.singletonList(upload));
      log.info("[ModelTrainHandler.getLabelPath] 上传文件到OSS成功，name={}", upload.getDestPath());
      DatasetDetailPO detailPO1 = new DatasetDetailPO();
      detailPO1.setUpdateDatetime(LocalDateTime.now());
      detailPO1.setId(detailPO.getId());
      datasetDetailService.updateById(detailPO1);
      return upload.getDestPath();
    } finally {
      if (!path.toFile().delete()) {
        log.warn("[ModelTrainHandler.getLabelPath] 删除临时文件失败，path={}", path);
      }
    }
  }

  /**
   * 生成自动标注结果中，高置信数据的标签结果，按照数据的ID排序
   *
   * @param writer 文件输出流
   * @param taskId 任务ID
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
      throw new BusinessIOException("生成高置信标签结果文件错误！", e);
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
            resultPO.setType("高置信数据");
            writer.write(objectMapper.writeValueAsString(resultPO));
            writer.newLine();
          }
        }
        // 根据训练集文件继续添加
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
            simple.setType("训练集数据");
            writer.write(objectMapper.writeValueAsString(simple));
            writer.newLine();
          }
        }
        log.info("[ModelTrainHandler.generateAutoLabelFile] 生成自动标注结果文件成功，taskId={}", taskId);
      } catch (IOException e) {
        throw new BusinessIOException("生成自动标注结果文件错误！", e);
      }
      File file = path.toFile();
      FileUpload upload = new FileUpload();
      upload.setFile(file);
      String parent = fileUploadProperties.getOssPrefix() + taskId + "/vote/";
      upload.setDestPath(parent + file.getName());
      upload.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      ossService.uploadFiles(Collections.singletonList(upload));
      log.info("[ModelTrainHandler.generateAutoLabelFile] 上传文件到OSS成功，name={}", upload.getDestPath());
      return upload.getDestPath();
    } finally {
      try {
        Files.delete(path);
      } catch (IOException e) {
        log.error("[ModelTrainHandler.generateAutoLabelFile] 删除文件失败", e);
      }
    }
  }

  @Override
  protected void internalHandle(AlgorithmTask task, DatasetInput datasetInput) {
    log.info("开始训练模型");
    send(task, datasetInput);
    log.info("训练模型结束");
  }

  @Override
  protected void handleOnError(AlgorithmTask task, Exception exception) {
    log.error("[ModelTrainHandler.handleOnError] 模型训练失败", exception);
    // 插入一条空白结果
    TrainCallbackDTO trainCallbackDTO = new TrainCallbackDTO();
    trainCallbackDTO.setTaskId(task.getTaskId());
    trainCallbackDTO.setRecordId(task.getRecordId());
    modelTrainService.saveFailedTrainResult(trainCallbackDTO);
    // websocket通知
    String msg;
    if (exception instanceof BusinessException) {
      msg = String.format("模型训练失败，错误信息：%s", exception.getMessage());
    } else {
      msg = String.format("模型训练失败，错误信息：%s！", "系统内部错误，请联系管理员");
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
    // 发送训练请求
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(1000 * 60).build();
    RequestConfigHolder.bind(requestConfig);
    try {
      Integer model = (Integer) task.getParams().get("model");
      if (model == null) {
        model = ModelType.LR.getType();
      }
      long timeOut;
      if (model == ModelType.LR.getType()) {
        // LR 一般两分钟没有回调就认为训练失败
        // 2022年5月23日14:27:31，LR训练超时时间改为原先时间的两倍
        timeOut = 2 * 60 * 1000 * 2;
      } else {
        timeOut = 30 * 60 * 1000 * 2;
      }
      String uid = (String) task.getParams().get(SESSION_UID);
      stringRedisTemplate.opsForValue()
          .set(getTaskTrainKey(task.getTaskId(), task.getRecordId()), uid, timeOut + 30 * 1000, TimeUnit.MILLISECONDS);
      ModelType type = ModelType.getFromType(model);
      String path = algorithmProperties.getTrainPath() + "?model=" + type.getPath();
      log.info("[ModelTrainHandler.send] 发送模型训练请求，模型类型：{}，请求地址：{}", type, path);
      log.info("[ModelTrainHandler.send] 模型训练请求参数：{}", datasetInput);
      ResponseEntity<String> responseEntity = restTemplate.postForEntity(path, datasetInput, String.class);
      log.info("训练结果：{}", responseEntity.getBody());
      // 添加回调超时监听任务
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
