package com.wl.xc.modelfun.tasks.algorithm.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import cn.hutool.core.lang.id.NanoId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wl.xc.modelfun.commons.RequestConfigHolder;
import com.wl.xc.modelfun.commons.constants.FileCacheConstant;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.AutoLabelType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIOException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.FileMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.TrainLabelDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.AutoLabelInput;
import com.wl.xc.modelfun.entities.model.DatasetInput;
import com.wl.xc.modelfun.entities.model.TrainLabelResult;
import com.wl.xc.modelfun.entities.po.IntegrateLabelResultPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.service.IntegrateLabelResultService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.PageUtil;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestClientResponseException;

/**
 * @version 1.0
 * @date 2022/4/20 11:45
 */
@Slf4j
@Component
public class AutoLabelHandler extends AbstractHandler {

  protected IntegrationRecordsService integrationRecordsService;

  private IntegrateLabelResultService integrateLabelResultService;

  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.AUTO_LABEL;
  }

  @Override
  protected DatasetInput generateDatasetInput(AlgorithmTask task) {
    AutoLabelInput autoLabelInput = new AutoLabelInput();
    // 规则集成的时候生成的模型文件地址
    IntegrationRecordsPO recordsPO = integrationRecordsService.getById(task.getRecordId());
    String modelAddress = ossService.getUrlSigned(recordsPO.getVoteModelAddress(), 1800 * 1000);
    autoLabelInput.setLabelModelPath(modelAddress);
    autoLabelInput.setMappingModelPath(
        ossService.getUrlSigned(recordsPO.getMappingModelAddress(), 1800 * 1000));
    // 从缓存中先查询，如果没有的话，再从数据库中查询
    String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(task.getTaskId());
    String unlabelMatrix =
        (String)
            stringRedisTemplate
                .opsForHash()
                .get(cacheKey, FileCacheConstant.INTEGRATE_UNLABEL_MATRIX);
    if (StringUtils.isBlank(unlabelMatrix)) {
      unlabelMatrix = getUnlabeledDataResult(task.getTaskId());
    }
    autoLabelInput.setTrainLabelMatrix(
        ossService.getUrlSigned(unlabelMatrix, fileUploadProperties.getExpireTime()));
    String testMatrix = getTestDataResult(task.getTaskId());
    stringRedisTemplate.opsForHash().put(cacheKey, FileCacheConstant.INTEGRATE_TEST_MATRIX, testMatrix);
    autoLabelInput.setTestLabelMatrix(
        ossService.getUrlSigned(testMatrix, fileUploadProperties.getExpireTime()));
    log.info("[AutoLabelHandler] 测试集标签矩阵文件地址：{}", autoLabelInput.getTestLabelMatrix());
    log.info("[AutoLabelHandler] 未标注集标签矩阵文件地址：{}", autoLabelInput.getTrainLabelMatrix());
    log.info("[AutoLabelHandler.generateDatasetInput] mapping_path:{}", autoLabelInput.getMappingModelPath());
    return autoLabelInput;
  }

  @Override
  protected void internalHandle(AlgorithmTask task, DatasetInput datasetInput) {
    send(task, datasetInput);
    // 删除未标注集矩阵文件
    String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(task.getTaskId());
    String unlabelMatrix =
        (String)
            stringRedisTemplate
                .opsForHash()
                .get(cacheKey, FileCacheConstant.INTEGRATE_UNLABEL_MATRIX);
    if (StringUtils.isNotBlank(unlabelMatrix)) {
      ossService.deleteFile(unlabelMatrix);
      stringRedisTemplate.opsForHash().delete(cacheKey, FileCacheConstant.INTEGRATE_UNLABEL_MATRIX);
      log.info("[AutoLabelHandler.internalHandle] 删除临时生成的未标注集矩阵文件：{}", unlabelMatrix);
    }
    // 删除测试集矩阵文件
    String testMatrix = (String) stringRedisTemplate.opsForHash()
        .get(cacheKey, FileCacheConstant.INTEGRATE_TEST_MATRIX);
    if (StringUtils.isNotBlank(testMatrix)) {
      ossService.deleteFile(testMatrix);
      stringRedisTemplate.opsForHash().delete(cacheKey, FileCacheConstant.INTEGRATE_TEST_MATRIX);
      log.info("[AutoLabelHandler.internalHandle] 删除临时生成的测试集矩阵文件：{}", testMatrix);
    }
    Map<String, Object> params = task.getParams();
    if (params != null) {
      TaskInfoPO po = taskInfoService.getById(task.getTaskId());
      WebsocketDTO dto = new WebsocketDTO();
      dto.setEvent(WsEventType.AUTO_LABEL_SUCCESS);
      dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), "自动标注成功", true));
      String uid = (String) params.get(SESSION_UID);
      WebSocketHandler.sendByUid(uid, dto);
    }
  }

  @Override
  protected void handleOnError(AlgorithmTask task, Exception exception) {
    log.error("[AutoLabelHandler.handleOnError] 自动标注失败", exception);
    Long recordId = task.getRecordId();
    IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
    integrationRecordsPO.setId(recordId);
    integrationRecordsPO.setLabeled(3);
    integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
    integrationRecordsService.updateById(integrationRecordsPO);
    String msg;
    if (exception instanceof BusinessException) {
      msg = String.format("自动标注失败，错误信息：%s", exception.getMessage());
    } else {
      msg = String.format("自动标注失败，错误信息：%s！", "系统内部错误，请联系管理员");
    }
    TaskInfoPO po = taskInfoService.getById(task.getTaskId());
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.AUTO_LABEL_FAIL);
    dto.setData(WebsocketDataDTO.create(task.getTaskId(), po.getName(), msg, false));
    String uid = (String) task.getParams().get(SESSION_UID);
    WebSocketHandler.sendByUid(uid, dto);
    String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(task.getTaskId());
    // 删除测试集矩阵文件
    String testMatrix = (String) stringRedisTemplate.opsForHash()
        .get(cacheKey, FileCacheConstant.INTEGRATE_TEST_MATRIX);
    if (StringUtils.isNotBlank(testMatrix)) {
      ossService.deleteFile(testMatrix);
      log.info("[AutoLabelHandler.handleOnError] 删除临时生成的测试集矩阵文件：{}", testMatrix);
    }
  }

  @Override
  protected void send(AlgorithmTask task, DatasetInput datasetInput) {
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(-1).build();
    RequestConfigHolder.bind(requestConfig);
    String result;
    StopWatch stopWatch = new StopWatch();
    try {
      stopWatch.start();
      log.info("[AutoLabelHandler.send] 发送自动标注请求");
      ResponseEntity<String> responseEntity =
          restTemplate.postForEntity(
              algorithmProperties.getAutoLabelPath(), datasetInput, String.class);
      stopWatch.stop();
      result = responseEntity.getBody();
      if (result == null) {
        throw new BusinessIllegalStateException("自动标注, 返回结果为空");
      }
      log.info("[AutoLabelHandler.send] 自动标注请求返回结果大小：{}", result.length());
    } catch (RestClientResponseException responseException) {
      throw new BusinessIllegalStateException(responseException.getMessage(), responseException);
    } finally {
      RequestConfigHolder.clear();
    }
    TrainLabelResult trainLabelResult = generateTrainFile(task.getTaskId(), result);
    log.info("[AutoLabelHandler.handle] 调用规则生成文件名：{}", trainLabelResult.getTrainFile());
    try {
      // 更新规则集成记录
      Long recordId = task.getRecordId();
      IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
      integrationRecordsPO.setId(recordId);
      integrationRecordsPO.setTaskId(task.getTaskId());
      integrationRecordsPO.setLabeled(2);
      integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsPO.setTrainLabelCount(trainLabelResult.getTrainLabelCount());
      integrationRecordsPO.setTrainSentenceCount(trainLabelResult.getTrainSentenceCount());
      integrationRecordsPO.setUnlabelCoverage(trainLabelResult.getUnlabelCoverage());
      integrationRecordsPO.setTestRecall(trainLabelResult.getTestRecall());
      integrationRecordsPO.setTestAccuracy(trainLabelResult.getTestAccuracy());
      integrationRecordsPO.setTestF1Score(trainLabelResult.getTestF1Score());
      integrationRecordsPO.setTimeCost((int) stopWatch.getTotalTimeSeconds());
      integrationRecordsPO.setResultFileAddress(trainLabelResult.getTestLabelResult());
      // 更新规则集成记录
      integrationRecordsService.updateById(integrationRecordsPO);
    } catch (Exception e) {
      ossService.deleteFile(trainLabelResult.getTrainFile());
      throw new RuntimeException(e);
    }
  }

  private TrainLabelResult generateTrainFile(Long taskId, String response) {
    List<Integer> unLabelDataList;
    List<Integer> certainList;
    List<Integer> uncertainList;
    TrainLabelDTO trainLabelDTO;
    try {
      trainLabelDTO = objectMapper.readValue(response, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException(e);
    }
    if (StringUtils.isBlank(trainLabelDTO.getTestLabel())) {
      log.error("[AutoLabelHandler.generateTrainFile] 自动标注没有返回测试集文件打标结果");
      throw new BusinessIllegalStateException("服务器内部错误，请联系管理员！");
    }
    // 未标注的打标结果
    String trainLabelPath = trainLabelDTO.getTrainLabel();
    // 高置信数据下标文件路径
    String certaintyIdx = trainLabelDTO.getCertaintyIdx();
    // 待审核数据下标路径
    String uncertaintyIdx = trainLabelDTO.getUncertaintyIdx();
    String tempPath = fileUploadProperties.getTempPath();
    Path path = Paths.get(tempPath, NanoId.randomNanoId() + ".json");
    Path certainPath = FileMethods.prepareFile(tempPath, NanoId.randomNanoId() + ".json");
    Path uncertainPath = FileMethods.prepareFile(tempPath, NanoId.randomNanoId() + ".json");
    try {
      ossService.download(trainLabelPath, path.toAbsolutePath().toString());
      ossService.download(certaintyIdx, certainPath.toAbsolutePath().toString());
      ossService.download(uncertaintyIdx, uncertainPath.toAbsolutePath().toString());
      unLabelDataList = objectMapper.readValue(path.toFile(), new TypeReference<>() {
      });
      certainList = objectMapper.readValue(certainPath.toFile(), new TypeReference<>() {
      });
      uncertainList = objectMapper.readValue(uncertainPath.toFile(), new TypeReference<>() {
      });
    } catch (IOException e) {
      throw new BusinessIOException("解析自动标注结果文件失败", e);
    } finally {
      try {
        Files.delete(path);
        Files.delete(certainPath);
        Files.delete(uncertainPath);
      } catch (IOException e) {
        log.error("[AutoLabelHandler.generateTrainFile] 删除临时文件失败！");
      }
      // 删除算法临时生成的文件
      ossService.deleteFile(trainLabelPath);
      ossService.deleteFile(certaintyIdx);
      ossService.deleteFile(uncertaintyIdx);
      ossService.deleteFile(trainLabelDTO.getValLabel());
    }
    log.info(
        "[AutoLabelHandler.generateTrainFile] 自动标注返回参数：Accuracy={}, F1Score={}, Recall={}",
        trainLabelDTO.getAccuracy(),
        trainLabelDTO.getF1(),
        trainLabelDTO.getRecall());
    List<LabelInfoPO> infoPOS = labelInfoService.selectListByTaskId(taskId);
    Map<Integer, String> labelMap = new HashMap<>(infoPOS.size());
    for (LabelInfoPO infoPO : infoPOS) {
      labelMap.put(infoPO.getLabelId(), infoPO.getLabelDesc());
    }
    TrainLabelResult trainLabelResult = getTrainLabelResult(taskId, unLabelDataList, labelMap, certainList,
        uncertainList);
    trainLabelResult.setTestAccuracy(
        CalcUtil.multiply(trainLabelDTO.getAccuracy().toString(), "100", 2));
    trainLabelResult.setTestF1Score(CalcUtil.multiply(trainLabelDTO.getF1().toString(), "100", 2));
    trainLabelResult.setTestRecall(
        CalcUtil.multiply(trainLabelDTO.getRecall().toString(), "100", 2));
    trainLabelResult.setTestLabelResult(trainLabelDTO.getTestLabel());
    return trainLabelResult;
  }

  private TrainLabelResult getTrainLabelResult(Long taskId, List<Integer> labelList, Map<Integer, String> labelMap,
      List<Integer> certainList, List<Integer> uncertainList) {
    Set<Integer> certainSet = new HashSet<>(certainList);
    Set<Integer> uncertainSet = new HashSet<>(uncertainList);
    long labeledDataCount = 0L;
    long unlabeledDataCount;
    Set<Integer> labelSet = new HashSet<>(labelMap.size());
    // 清除旧的训练结果
    integrateLabelResultService.deleteByTaskId(taskId);
    unlabeledDataCount = unlabelDataService.countUnlabelDataByTaskId(taskId);
    long totalPage = PageUtil.totalPage(unlabeledDataCount, 10000L);
    // 每次从数据库中取10000条数据进行计算，防止内存溢出
    int count = 0;
    for (int i = 0; i < totalPage; i++) {
      long offset = i * 10000L;
      List<UnlabelDataPO> unlabelDataPOList =
          unlabelDataService.pageByTaskId(taskId, offset, 10000);
      List<IntegrateLabelResultPO> list = new ArrayList<>(2000);
      for (UnlabelDataPO dataPO : unlabelDataPOList) {
        Integer labeled;
        int type;
        if (certainSet.contains(count)) {
          // 高置信数据
          labeled = labelList.get(count);
          type = AutoLabelType.CORRECT.getType();
        } else if (uncertainSet.contains(count)) {
          // 待审核数据
          labeled = labelList.get(count);
          type = AutoLabelType.DOUBTFUL.getType();
        } else {
          count++;
          continue;
        }
        count++;
        if (labeled < 0 || !labelMap.containsKey(labeled)) {
          continue;
        }
        IntegrateLabelResultPO integrateLabelResultPO = new IntegrateLabelResultPO();
        integrateLabelResultPO.setSentenceId(dataPO.getDataId());
        integrateLabelResultPO.setSentence(dataPO.getSentence());
        integrateLabelResultPO.setLabelId(labeled);
        integrateLabelResultPO.setLabelDes(labelMap.get(labeled));
        integrateLabelResultPO.setTaskId(taskId);
        integrateLabelResultPO.setDataType(type);
        list.add(integrateLabelResultPO);
        labeledDataCount++;
        labelSet.add(labeled);
      }
      if (list.size() > 0) {
        integrateLabelResultService.saveBatchNoLog(list);
      }
    }
    TrainLabelResult result = new TrainLabelResult();
    result.setTrainSentenceCount(labeledDataCount);
    if (unlabeledDataCount > 0) {
      result.setUnlabelCoverage(
          CalcUtil.multiply(CalcUtil.divide(labeledDataCount, unlabeledDataCount, 2), "100", 2));
    } else {
      result.setUnlabelCoverage("0.00");
    }
    result.setTrainLabelCount(labelSet.size());
    return result;
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setIntegrateLabelResultService(
      IntegrateLabelResultService integrateLabelResultService) {
    this.integrateLabelResultService = integrateLabelResultService;
  }
}
