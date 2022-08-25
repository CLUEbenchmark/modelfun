package com.wl.xc.modelfun.tasks.algorithm.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskTrainKey;

import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.AutoLabelType;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.AlgorithmMethods;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.DatasetInput;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.model.NerDatasetInput;
import com.wl.xc.modelfun.entities.model.NerLabelDataWithType;
import com.wl.xc.modelfun.entities.model.NerLabelDataWithType.EntitiesDTO;
import com.wl.xc.modelfun.entities.model.NerUnLabel;
import com.wl.xc.modelfun.entities.model.TrainCallbackEvent;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.NerAutoLabelTrainPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.TrainRecordsPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.entities.req.DatasetDetailReq;
import com.wl.xc.modelfun.entities.vo.NerDataLabelDataVO;
import com.wl.xc.modelfun.entities.vo.NerDataLabelDataVO.LabelsDTO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.service.NerAutoLabelResultService;
import com.wl.xc.modelfun.service.NerAutoLabelTrainService;
import com.wl.xc.modelfun.service.NerService;
import com.wl.xc.modelfun.service.NerTestDataService;
import com.wl.xc.modelfun.service.TrainRecordsService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.daemon.TrainResultCallbackListener;
import com.wl.xc.modelfun.utils.PageUtil;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * @version 1.0
 * @date 2022/6/9 14:13
 */
@Slf4j
@Component
public class NerTrainHandler extends AbstractHandler {

  private NerTestDataService nerTestDataService;

  private NerAutoLabelResultService nerAutoLabelResultService;

  private TrainRecordsService trainRecordsService;

  private TrainResultCallbackListener trainResultCallbackListener;

  private NerAutoLabelTrainService nerAutoLabelTrainService;

  private NerService nerService;

  @Override
  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.NER_TRAIN;
  }

  @Override
  public void handle(AlgorithmTask task) {
    log.info("[NerTrainHandler.handle] 开始NER模型训练");
    try {
      Long taskId = task.getTaskId();
      TaskInfoPO taskInfoPO = taskInfoService.getById(taskId);
      if (taskInfoPO == null) {
        throw new BusinessIllegalStateException("任务id为" + taskId + "的任务不存在");
      }
      List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(taskId);
      Map<Integer, String> labelMap = labelInfoPOS.stream()
          .collect(Collectors.toMap(LabelInfoPO::getLabelId, LabelInfoPO::getLabelDesc));
      NerDatasetInput datasetInput = new NerDatasetInput();
      datasetInput.setNumClass((long) labelInfoPOS.size());
      datasetInput.setDescription(taskInfoPO.getDescription());
      datasetInput.setDomainType(taskInfoPO.getDomain());
      datasetInput.setKeywords(taskInfoPO.getKeyword());
      datasetInput.setName(taskInfoPO.getName());
      datasetInput.setTaskType(taskInfoPO.getTaskType().toString());
      TrainRecordsPO recordsPO = trainRecordsService.getById(task.getRecordId());
      if (recordsPO == null) {
        throw new BusinessIllegalStateException("训练记录id为" + task.getRecordId() + "的记录不存在");
      }
      // 生成训练集全集文件，并且更新训练记录
      String trainFile = generateTrainFile(taskId, datasetInput, recordsPO);
      List<String> schemas = new ArrayList<>(labelMap.values());
      datasetInput.setSchemas(schemas);
      datasetInput.setTaskId(taskId);
      datasetInput.setRecordId(task.getRecordId());
      String url =
          AlgorithmMethods.generateUrl(
              algorithmProperties.getAlgorithmCallbackUrl(), CallBackAction.NER_TRAIN);
      datasetInput.setCallback(url);
      recordsPO.setTrainFile(trainFile);
      recordsPO.setUpdateDatetime(LocalDateTime.now());
      trainRecordsService.updateById(recordsPO);
      send(task, datasetInput);
    } catch (Exception e) {
      handleOnError(task, e);
    }

  }

  @Override
  protected DatasetInput generateDatasetInput(AlgorithmTask task) {
    throw new UnsupportedOperationException("不支持该方法");
  }

  @Override
  protected void internalHandle(AlgorithmTask task, DatasetInput datasetInput) {
    throw new UnsupportedOperationException("不支持该方法");
  }

  @Override
  protected void handleOnError(AlgorithmTask task, Exception exception) {
    log.error("[NerTrainHandler.handleOnError]", exception);
    // 插入一条空白结果
    Long recordId = task.getRecordId();
    TrainRecordsPO recordsPO = new TrainRecordsPO();
    recordsPO.setId(recordId);
    recordsPO.setTrainStatus(2);
    recordsPO.setUpdateDatetime(LocalDateTime.now());
    trainRecordsService.updateById(recordsPO);
    // websocket通知
    String msg;
    if (exception instanceof BusinessException) {
      msg = String.format("模型训练失败，错误信息：%s", exception.getMessage());
    } else {
      msg = String.format("模型训练失败，错误信息：%s！", "系统内部错误，请联系管理员");
    }
    String uid = (String) task.getParams().get(SESSION_UID);
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.TRAIN_FAIL);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), msg, false));
    WebSocketHandler.sendByUid(uid, dto);
  }

  @Override
  protected void send(AlgorithmTask task, DatasetInput datasetInput) {
    log.info("[NerTrainHandler.send] 发送NER模型训练请求。url:{}", algorithmProperties.getNerTrainUrl());
    log.info("[NerTrainHandler.send] 请求体:{}", datasetInput);
    long timeOut = 3600 * 1000 * 12;
    try {
      String uid = (String) task.getParams().get(SESSION_UID);
      stringRedisTemplate.opsForValue()
          .set(getTaskTrainKey(task.getTaskId(), task.getRecordId()), uid, timeOut + 30 * 1000, TimeUnit.MILLISECONDS);
      ResponseEntity<String> responseEntity =
          restTemplate.postForEntity(
              algorithmProperties.getNerTrainUrl(), datasetInput, String.class);
      log.info("训练结果：{}", responseEntity.getBody());
      // 添加回调超时监听任务
      TrainCallbackEvent event = new TrainCallbackEvent();
      event.setRecordId(task.getRecordId());
      event.setDelayMillisecond(timeOut);
      event.setTaskId(task.getTaskId());
      trainResultCallbackListener.addCallbackListener(event);
    } catch (RestClientResponseException e) {
      throw new BusinessIllegalStateException(e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("服务内部错误！", e);
    }
  }

  /**
   * 生成本次训练的训练集文件，提供给用户下载
   *
   * @param taskId       任务id
   * @param datasetInput 标签map
   * @param recordsPO
   */
  private String generateTrainFile(Long taskId, NerDatasetInput datasetInput, TrainRecordsPO recordsPO) {
    Long count = nerTestDataService.countByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
    DatasetDetailReq req = new DatasetDetailReq();
    req.setTaskId(taskId);
    req.setCurPage(1L);
    req.setPageSize(count);
    req.setDesc(false);
    PageVO<NerDataLabelDataVO> pageVO = nerService.pageNerTrainData(req);
    AtomicInteger entityId = new AtomicInteger(0);
    List<NerLabelDataWithType> trainDataModels = pageVO.convert(p -> convert(p, entityId)).getRecords();
    // 写训练集文件
    DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
    String fileAddress = detailPO.getFileAddress();
    Path path = prepareFile(taskId);
    writeTestOrTrainFile(trainDataModels, path, fileAddress);
    // 写测试集文件
    detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_UN_SHOW.getType());
    count = nerTestDataService.countByTaskIdAndType(taskId, DatasetType.TEST.getType());
    req.setPageSize(count);
    pageVO = nerService.pageNerTestData(req);
    AtomicInteger testEntityId = new AtomicInteger(0);
    List<NerLabelDataWithType> testDataModels = pageVO.convert(p -> convert(p, testEntityId)).getRecords();
    path = prepareFile(taskId);
    writeTestOrTrainFile(testDataModels, path, detailPO.getFileAddress());
    datasetInput.setTestPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000 * 12));
    // 写下载文件
    trainDataModels.forEach(dataModel -> dataModel.setType("训练集"));
    // 获取自动标注结果
    count = nerAutoLabelResultService.countByTaskIdAndType(taskId, AutoLabelType.CORRECT.getType());
    if (count > 0) {
      req.setPageSize(count);
      req.setDataType(AutoLabelType.CORRECT.getType());
      pageVO = nerService.pageNerLabelResult(req);
      List<NerAutoLabelTrainPO> labelTrainPOS = nerAutoLabelTrainService.selectByTaskId(taskId);
      // 需要写入训练集的数据的id
      Set<Long> collect = labelTrainPOS.stream().map(NerAutoLabelTrainPO::getDataId).collect(Collectors.toSet());
      pageVO.getRecords().removeIf(p -> !collect.contains(p.getDataId()));
      // 写入训练集的结果
      List<NerLabelDataWithType> correct = pageVO.convert(p -> convert(p, entityId)).getRecords();
      correct.forEach(dataModel -> dataModel.setType("高置信数据"));
      trainDataModels.addAll(correct);
    }
    recordsPO.setTrainCount(trainDataModels.size());
    // 待审核数据，2022年6月14日14:19:39去除
    /*count = nerAutoLabelResultService.countByTaskIdAndType(taskId, AutoLabelType.DOUBTFUL.getType());
    if (count > 0) {
      req.setPageSize(count);
      req.setDataType(AutoLabelType.DOUBTFUL.getType());
      pageVO = nerService.pageNerLabelResult(req);
      // 正确的结果
      entityId.set(0);
      List<NerLabelDataWithType> doubtful = pageVO.convert(p -> convert(p, entityId)).getRecords();
      doubtful.forEach(dataModel -> dataModel.setType("待审核数据"));
      trainDataModels.addAll(doubtful);
    }*/
    path = prepareFile(taskId);
    fileAddress = fileUploadProperties.getOssPrefix() + taskId + "/vote/" + path.getFileName();
    String downloadFile = writeVoteFile(trainDataModels, path, fileAddress);
    datasetInput.setTrainPath(ossService.getUrlSigned(downloadFile, 3600 * 1000 * 12));
    // 写未标注集文件
    detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.UNLABELED.getType());
    writeUnlabelFile(taskId, detailPO.getFileAddress());
    datasetInput.setUnlabeledPath(ossService.getUrlSigned(detailPO.getFileAddress(), 3600 * 1000 * 12));
    return downloadFile;
  }

  private void writeTestOrTrainFile(List<NerLabelDataWithType> dataModels, Path path, String fileAddress) {
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        int len = dataModels.size();
        for (int i = 0; i < len; i++) {
          NerLabelDataWithType dataModel = dataModels.get(i);
          writer.write(objectMapper.writeValueAsString(dataModel));
          if (i != len - 1) {
            writer.newLine();
          }
        }
      } catch (Exception e) {
        log.error("写文件失败", e);
      }
      FileUpload fileUpload = new FileUpload();
      fileUpload.setFile(path.toFile());
      fileUpload.setDestPath(fileAddress);
      ossService.uploadFiles(Collections.singletonList(fileUpload));
    } finally {
      try {
        Files.deleteIfExists(path);
      } catch (Exception e) {
        log.error("删除文件失败", e);
      }
    }
  }

  private String writeVoteFile(List<NerLabelDataWithType> dataModels, Path path, String fileAddress) {
    try {
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        int len = dataModels.size();
        long sentenceId = 0;
        int entities = 0;
        for (int i = 0; i < len; i++) {
          NerLabelDataWithType dataModel = dataModels.get(i);
          dataModel.setId(sentenceId++);
          List<EntitiesDTO> dtos = dataModel.getEntities();
          if (dtos != null && dtos.size() > 0) {
            for (EntitiesDTO dto : dtos) {
              dto.setId(entities++);
            }
          }
          writer.write(objectMapper.writeValueAsString(dataModel));
          if (i != len - 1) {
            writer.newLine();
          }
        }
      } catch (Exception e) {
        log.error("写文件失败", e);
      }
      FileUpload fileUpload = new FileUpload();
      fileUpload.setFile(path.toFile());
      fileUpload.setDestPath(fileAddress);
      fileUpload.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      ossService.uploadFiles(Collections.singletonList(fileUpload));
      return fileAddress;
    } finally {
      try {
        Files.deleteIfExists(path);
      } catch (Exception e) {
        log.error("删除文件失败", e);
      }
    }
  }

  private void writeUnlabelFile(Long taskId, String fileAddress) {
    Path path = prepareFile(taskId);
    try {
      Long total = unlabelDataService.countUnlabelDataByTaskId(taskId);
      long totalPage = PageUtil.totalPage(total, 10000L);
      try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
        for (long i = 0; i < totalPage; i++) {
          List<UnlabelDataPO> unlabelDataPOS = unlabelDataService.pageByTaskId(taskId, i * 10000L, 10000);
          for (UnlabelDataPO unlabelDataPO : unlabelDataPOS) {
            NerUnLabel unLabel = new NerUnLabel();
            unLabel.setText(unlabelDataPO.getSentence());
            writer.write(objectMapper.writeValueAsString(unLabel));
            writer.newLine();
          }
        }
      } catch (IOException e) {
        log.error("写文件失败", e);
      }
      FileUpload fileUpload = new FileUpload();
      fileUpload.setFile(path.toFile());
      fileUpload.setDestPath(fileAddress);
      ossService.uploadFiles(Collections.singletonList(fileUpload));
    } finally {
      try {
        Files.deleteIfExists(path);
      } catch (Exception e) {
        log.error("删除文件失败", e);
      }
    }
  }

  private NerLabelDataWithType convert(NerDataLabelDataVO vo, AtomicInteger entityId) {
    NerLabelDataWithType model = new NerLabelDataWithType();
    model.setId(vo.getDataId());
    model.setText(vo.getSentence());
    model.setRelations(Collections.emptyList());
    List<LabelsDTO> labels = vo.getLabels();
    List<EntitiesDTO> entities = labels.stream().map(l -> {
      EntitiesDTO dto = new EntitiesDTO();
      dto.setLabel(l.getLabelDes());
      dto.setStartOffset(l.getStartOffset());
      dto.setEndOffset(l.getEndOffset());
      dto.setId(entityId.getAndIncrement());
      return dto;
    }).collect(Collectors.toList());
    model.setEntities(entities);
    return model;
  }

  @Autowired
  public void setNerTestDataService(NerTestDataService nerTestDataService) {
    this.nerTestDataService = nerTestDataService;
  }

  @Autowired
  public void setNerAutoLabelResultService(NerAutoLabelResultService nerAutoLabelResultService) {
    this.nerAutoLabelResultService = nerAutoLabelResultService;
  }

  @Autowired
  public void setTrainRecordsService(TrainRecordsService trainRecordsService) {
    this.trainRecordsService = trainRecordsService;
  }

  @Autowired
  public void setNerService(NerService nerService) {
    this.nerService = nerService;
  }

  @Autowired
  public void setTrainResultCallbackListener(
      TrainResultCallbackListener trainResultCallbackListener) {
    this.trainResultCallbackListener = trainResultCallbackListener;
  }

  @Autowired
  public void setNerAutoLabelTrainService(NerAutoLabelTrainService nerAutoLabelTrainService) {
    this.nerAutoLabelTrainService = nerAutoLabelTrainService;
  }
}
