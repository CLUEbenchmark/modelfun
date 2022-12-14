package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.UPDATE_TIME;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.AUTO_LABEL_FAILED;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.AUTO_LABEL_RUNNING;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.INTEGRATION_NOT_EXIT;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.INTEGRATION_RUNNING;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.NOT_AUTO_LABELED;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.NOT_EXIST;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.TRAIN_RUNNING;

import cn.hutool.core.lang.id.NanoId;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.constants.CommonConstant;
import com.wl.xc.modelfun.commons.constants.FileCacheConstant;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.ModelType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.FileMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.CommonProperties;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO.Arg;
import com.wl.xc.modelfun.entities.dto.TrainCallbackDTO;
import com.wl.xc.modelfun.entities.model.FileUpload;
import com.wl.xc.modelfun.entities.model.LabelResultDetail;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.model.PredictDetail;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.NerTrainLabelDetailPO;
import com.wl.xc.modelfun.entities.po.NerTrainLabelResultPO;
import com.wl.xc.modelfun.entities.po.RuleOverviewPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.po.TrainLabelSentenceInfoPO;
import com.wl.xc.modelfun.entities.po.TrainRecordsPO;
import com.wl.xc.modelfun.entities.po.TrainResultPO;
import com.wl.xc.modelfun.entities.po.TrainResultWithRecordPO;
import com.wl.xc.modelfun.entities.req.MatrixDetailReq;
import com.wl.xc.modelfun.entities.req.ModelTrainReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelPageReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelReq;
import com.wl.xc.modelfun.entities.req.TrainReq;
import com.wl.xc.modelfun.entities.vo.MatrixVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TextTrainLabelDiffVO;
import com.wl.xc.modelfun.entities.vo.TrainResultVO;
import com.wl.xc.modelfun.service.IntegrateLabelResultService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.ModelTrainService;
import com.wl.xc.modelfun.service.NerTrainLabelDetailService;
import com.wl.xc.modelfun.service.NerTrainLabelResultService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleOverviewService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.TrainLabelSentenceInfoService;
import com.wl.xc.modelfun.service.TrainRecordsService;
import com.wl.xc.modelfun.service.TrainResultService;
import com.wl.xc.modelfun.tasks.algorithm.AlgoTaskAppendEventPublisher;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.daemon.TrainResultCallbackListener;
import com.wl.xc.modelfun.utils.BeanCopyUtil;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/4/12 13:28
 */
@Slf4j
@Service
public class ModelTrainServiceImpl implements ModelTrainService {

  private TrainResultService trainResultService;

  private TrainRecordsService trainRecordsService;

  private IntegrationRecordsService integrationRecordsService;

  private AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher;

  private RuleOverviewService ruleOverviewService;

  private IntegrateLabelResultService integrateLabelResultService;

  private LabelInfoService labelInfoService;

  private RuleInfoService ruleInfoService;

  private TaskInfoService taskInfoService;

  private TrainResultCallbackListener trainResultCallbackListener;

  private StringRedisTemplate stringRedisTemplate;

  private OssService ossService;

  private TestDataService testDataService;

  private NerTrainLabelResultService nerTrainLabelResultService;

  private TrainLabelSentenceInfoService trainLabelSentenceInfoService;

  private NerTrainLabelDetailService nerTrainLabelDetailService;

  private FileUploadProperties fileUploadProperties;

  private CommonProperties commonProperties;

  private ObjectMapper objectMapper;

  @Override
  public PageVO<TrainResultVO> getTrainRecordPage(ModelTrainReq req) {
    // ??????????????????????????????????????????????????????
    Page<TrainResultPO> page = Page.of(req.getCurPage(), req.getPageSize());
    PageVO<TrainResultWithRecordPO> records =
        trainResultService.pageByTaskId(page, req.getTaskId());
    if (records.getTotalRows() == 0) {
      return new PageVO<>();
    }
    List<TrainResultWithRecordPO> list = records.getRecords();
    List<TrainResultVO> result = list.stream().map(this::convert).collect(Collectors.toList());
    return new PageVO<>(
        records.getCurPage(),
        records.getPageSize(),
        records.getTotalRows(),
        records.getPageCount(),
        result);
  }

  /**
   * <pre>
   *   ???????????????
   *   ?????????????????????????????????????????????
   *     ???????????????
   *   ???????????????????????????
   *     1. ????????????????????????????????????
   *     2. ????????????????????????????????????????????????????????????
   *     3. ???????????????????????????????????????
   *       3.1 ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
   *       3.2 ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
   * </pre>
   *
   * @param req ????????????
   * @return ????????????
   */
  @Override
  public ResultVo<String> train(TrainReq req) {
    // ??????????????????????????????????????????????????????????????????????????????????????????????????????
    IntegrationRecordsPO lastIntegrationRecord = integrationRecordsService.getLastIntegrationRecord(req.getTaskId());
    if (lastIntegrationRecord == null) {
      return ResultVo.create(INTEGRATION_NOT_EXIT, false, null);
    }
    if (lastIntegrationRecord.getIntegrateStatus() == 0) {
      return ResultVo.create(INTEGRATION_RUNNING, false, null);
    }
    // ???????????????????????????????????????????????????????????????????????????
    if (lastIntegrationRecord.getLabeled() == 3) {
      return ResultVo.create(AUTO_LABEL_FAILED, false, null);
    }
    // ?????????????????????
    if (lastIntegrationRecord.getLabeled() == 1) {
      return ResultVo.create(AUTO_LABEL_RUNNING, false, null);
    }
    // ??????????????????
    if (lastIntegrationRecord.getLabeled() == 0) {
      return ResultVo.create(NOT_AUTO_LABELED, false, null);
    }
    // ??????????????????????????????????????????????????????
    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    TrainRecordsPO lastTrainRecord = trainRecordsService.getLastTrainRecord(req.getTaskId());
    if (lastTrainRecord == null) {
      // ??????????????????????????????????????????
      return ResultVo.createSuccess(commitTrainTask(req, lastIntegrationRecord, null));
    }
    if (lastTrainRecord.getTrainStatus() == 0) {
      // ?????????
      return ResultVo.create(TRAIN_RUNNING, false, null);
    }
    // ????????????????????????????????????????????????????????????
    if (lastTrainRecord.getTrainStatus() == 2) {
      return ResultVo.createSuccess(
          commitTrainTask(req, lastIntegrationRecord, lastIntegrationRecord.getCreateDatetime()));
    }
    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    // 2022???5???23???16:15:22???????????????????????????????????????????????????????????????
    /*if (lastTrainRecord.getUpdateDatetime().isAfter(lastIntegrationRecord.getUpdateDatetime())) {
      return ResultVo.create(TRAIN_NOT_MODIFY, false, null);
    }*/
    return ResultVo.createSuccess(
        commitTrainTask(req, lastIntegrationRecord, lastIntegrationRecord.getCreateDatetime()));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveTrainResult(TrainResultPO trainResultPO) {
    Long taskId = trainResultPO.getTaskId();
    Long trainRecordId = trainResultPO.getTrainRecordId();
    // ????????????????????????
    trainResultService.save(trainResultPO);
    // ????????????????????????
    TrainRecordsPO latestRecord = trainRecordsService.selectLatestDataVersion(taskId);
    int version = 1;
    if (latestRecord != null) {
      version = Integer.parseInt(latestRecord.getDataVersion()) + 1;
    }
    TrainRecordsPO po = new TrainRecordsPO();
    po.setTaskId(taskId);
    po.setId(trainRecordId);
    po.setTrainStatus(1);
    po.setDataVersion(String.valueOf(version));
    po.setUpdateDatetime(LocalDateTime.now());
    trainRecordsService.updateById(po);
  }

  @Override
  public ResultVo<Boolean> existRunningTrain(Long taskId) {
    TrainRecordsPO lastTrainRecord = trainRecordsService.getLastTrainRecord(taskId);
    if (lastTrainRecord == null) {
      // ?????????????????????
      return ResultVo.createSuccess(false);
    }
    if (lastTrainRecord.getTrainStatus() == 0) {
      // ?????????
      return ResultVo.createSuccess(true);
    } else {
      return ResultVo.createSuccess(false);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> saveTrainResultAsync(TrainCallbackDTO trainCallbackDTO) {
    log.info(
        "[ModelTrainServiceImpl.saveTrainResultAsync] ?????????????????????????????????accuracy:{},precision:{},recall:{},f1:{}",
        trainCallbackDTO.getAccuracy(),
        trainCallbackDTO.getPrecision(),
        trainCallbackDTO.getRecall(),
        trainCallbackDTO.getF1());
    trainResultCallbackListener.removeCallbackListener(trainCallbackDTO.getRecordId());
    try {
      if (Boolean.FALSE.equals(trainCallbackDTO.getState())) {
        log.error("[ModelTrainServiceImpl.saveTrainResultAsync] ?????????????????????detail:{}", trainCallbackDTO.getDetail());
        saveFailedTrainResult(trainCallbackDTO);
        return ResultVo.createSuccess(false);
      }
      TrainRecordsPO trainRecordsPO = trainRecordsService.getById(trainCallbackDTO.getRecordId());
      TrainResultPO trainResultPO = new TrainResultPO();
      trainResultPO.setTaskId(trainCallbackDTO.getTaskId());
      trainResultPO.setTrainRecordId(trainCallbackDTO.getRecordId());
      RuleOverviewPO overview = ruleOverviewService.getRuleOverviewByTaskId(trainCallbackDTO.getTaskId());
      if (overview != null) {
        trainResultPO.setCoverage(overview.getCoverage());
      }
      saveTrainAnalyze(trainCallbackDTO, trainRecordsPO);
      // ????????????????????????
      trainResultPO.setAccuracy(CalcUtil.multiply(trainCallbackDTO.getAccuracy() + "", "100"));
      // ????????????????????????
      trainResultPO.setTrainPrecision(CalcUtil.multiply(trainCallbackDTO.getPrecision() + "", "100"));
      // ????????????????????????
      trainResultPO.setRecall(CalcUtil.multiply(trainCallbackDTO.getRecall() + "", "100"));
      // F1 ??????????????????
      trainResultPO.setF1Score(CalcUtil.multiply(trainCallbackDTO.getF1() + "", "100"));
      // oss???????????????????????????
      trainResultPO.setFileAddress(trainCallbackDTO.getUrl());
      // ????????????
      trainResultPO.setLabelTypeCount(trainRecordsPO.getLabelCount());
      // ????????????
      trainResultPO.setRuleCount(trainRecordsPO.getRuleCount());
      // ???????????????
      trainResultPO.setTrainCount(trainRecordsPO.getTrainCount());
      trainResultPO.setModuleType(2);
      // ???????????????????????????
      List<List<Integer>> confusionMx = trainCallbackDTO.getConfusionMx();
      trainResultPO.setConfusionMx(objectMapper.writeValueAsString(confusionMx));
      saveTrainResult(trainResultPO);
      log.info("[ModelTrainServiceImpl.saveTrainResultAsync] ????????????????????????");
    } catch (Exception e) {
      log.error("[ModelTrainServiceImpl.saveTrainResultAsync]", e);
      trainCallbackDTO.setStatus(2);
      trainCallbackDTO.setDetail("????????????????????????");
      saveFailedTrainResult(trainCallbackDTO);
      return ResultVo.createSuccess(false);
    } finally {
      // ??????????????????
      String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(trainCallbackDTO.getTaskId());
      String autoResult = (String) stringRedisTemplate.opsForHash()
          .get(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT);
      if (StringUtils.isNotBlank(autoResult)) {
        ossService.deleteFile(autoResult);
        stringRedisTemplate.opsForHash().delete(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT);
      }
      String trainPath = (String) stringRedisTemplate.opsForHash().get(cacheKey, FileCacheConstant.TRAIN_PATH);
      if (StringUtils.isNotBlank(trainPath)) {
        ossService.deleteFile(trainPath);
        stringRedisTemplate.opsForHash().delete(cacheKey, FileCacheConstant.TRAIN_PATH);
      }
    }
    return ResultVo.createSuccess(true);
  }

  /**
   * ???????????????????????????????????????
   *
   * @param trainCallbackDTO ????????????
   * @param po               ????????????????????????
   */
  private void saveTrainAnalyze(TrainCallbackDTO trainCallbackDTO, TrainRecordsPO po) {
    Long taskId = trainCallbackDTO.getTaskId();
    Map<String, Arg> report = trainCallbackDTO.getReport();
    List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(po.getTaskId());
    // ??????ID????????????????????????
    Map<Integer, String> map = labelInfoPOS.stream()
        .collect(Collectors.toMap(LabelInfoPO::getLabelId, LabelInfoPO::getLabelDesc));
    List<NerTrainLabelResultPO> nerTrainLabelResultPOs = new ArrayList<>();
    for (Integer s : map.keySet()) {
      Arg arg = report.get(s.toString());
      if (arg == null) {
        continue;
      }
      NerTrainLabelResultPO resultPO = new NerTrainLabelResultPO();
      resultPO.setTrainPrecision(CalcUtil.multiply(arg.getPrecision() + "", "100"));
      String multiply = CalcUtil.multiply(arg.getRecall() + "", arg.getSupport() + "", 0, true);
      resultPO.setErrorCount(Math.max(arg.getSupport() - Integer.parseInt(multiply), 0));
      resultPO.setTrainRecordId(po.getId());
      resultPO.setRecall(CalcUtil.multiply(arg.getRecall() + "", "100"));
      resultPO.setLabelDes(map.get(s));
      resultPO.setSamples(arg.getSupport());
      nerTrainLabelResultPOs.add(resultPO);
    }
    // ??????????????????
    nerTrainLabelResultService.saveBatchNoLog(nerTrainLabelResultPOs);
    Map<String, Long> labelTrainId = nerTrainLabelResultPOs.stream()
        .collect(Collectors.toMap(NerTrainLabelResultPO::getLabelDes, NerTrainLabelResultPO::getId));
    // ?????????????????????????????????
    List<Integer> preds = trainCallbackDTO.getPreds();
    int size = preds.size();
    List<TestDataPO> testDataPOList = testDataService.getAllUnShowByTaskId(taskId);
    // ????????????
    List<TrainLabelSentenceInfoPO> labelSentenceInfoPOS = new ArrayList<>();
    List<NerTrainLabelDetailPO> nerTrainLabelDetailPOS = new ArrayList<>();
    for (int i = 0; i < testDataPOList.size(); i++) {
      TestDataPO testDataPO = testDataPOList.get(i);
      int label = i <= size ? preds.get(i) : -1;
      TrainLabelSentenceInfoPO sentenceInfoPO = new TrainLabelSentenceInfoPO();
      sentenceInfoPO.setSentence(testDataPO.getSentence());
      sentenceInfoPO.setDataId(testDataPO.getDataId());
      sentenceInfoPO.setTrainRecordId(po.getId());
      sentenceInfoPO.setLabelActual(testDataPO.getLabelDes());
      sentenceInfoPO.setLabelPredict(map.get(label));
      labelSentenceInfoPOS.add(sentenceInfoPO);
      if (testDataPO.getLabel() != label) {
        // ?????????????????????????????????????????????
        NerTrainLabelDetailPO detailPO = new NerTrainLabelDetailPO();
        Long trainLabelId = labelTrainId.get(testDataPO.getLabelDes());
        detailPO.setTrainLabelId(trainLabelId);
        detailPO.setDataId(testDataPO.getDataId());
        nerTrainLabelDetailPOS.add(detailPO);
      }
    }
    // ????????????????????????????????????
    if (labelSentenceInfoPOS.size() > 0) {
      trainLabelSentenceInfoService.saveBatchNoLog(labelSentenceInfoPOS);
    }
    // ??????????????????????????????
    if (nerTrainLabelDetailPOS.size() > 0) {
      nerTrainLabelDetailService.saveBatchNoLog(nerTrainLabelDetailPOS);
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveFailedTrainResult(TrainCallbackDTO trainCallbackDTO) {
    TrainResultPO trainResultPO = new TrainResultPO();
    trainResultPO.setTaskId(trainCallbackDTO.getTaskId());
    trainResultPO.setTrainRecordId(trainCallbackDTO.getRecordId());
    RuleOverviewPO overview = ruleOverviewService.getRuleOverviewByTaskId(trainCallbackDTO.getTaskId());
    if (overview != null) {
      trainResultPO.setCoverage(overview.getCoverage());
    }
    TrainRecordsPO trainRecordsPO = trainRecordsService.getById(trainCallbackDTO.getRecordId());
    // ???????????????
    trainResultPO.setTrainCount(trainRecordsPO.getTrainCount());
    // ????????????
    trainResultPO.setLabelTypeCount(trainRecordsPO.getLabelCount());
    // ????????????
    trainResultPO.setRuleCount(trainRecordsPO.getRuleCount());
    // ??????????????????
    trainResultService.save(trainResultPO);
    TrainRecordsPO po = new TrainRecordsPO();
    po.setId(trainCallbackDTO.getRecordId());
    po.setTrainStatus(trainCallbackDTO.getStatus());
    po.setUpdateDatetime(LocalDateTime.now());
    trainRecordsService.updateById(po);
  }

  @Override
  public PageResultVo<List<TextTrainLabelDiffVO>> getTrainLabelInfoDiff(NerTrainLabelPageReq req) {
    Page<NerTrainLabelDetailPO> page = Page.of(req.getCurPage(), req.getPageSize());
    PageVO<NerTrainLabelDetailPO> poPageVO = nerTrainLabelDetailService.pageByTrainLabelId(page,
        req.getRecordId());
    List<NerTrainLabelDetailPO> detailPOS = poPageVO.getRecords();
    if (detailPOS == null || detailPOS.isEmpty()) {
      return PageResultVo.createSuccess(poPageVO.convert(p -> new TextTrainLabelDiffVO()));
    }
    List<Long> collect = detailPOS.stream().map(NerTrainLabelDetailPO::getDataId).collect(Collectors.toList());
    List<TrainLabelSentenceInfoPO> labelSentenceInfoPOS = trainLabelSentenceInfoService.selectByTrainRecordId(
        req.getTrainRecordId(), collect);
    List<TextTrainLabelDiffVO> vos = new ArrayList<>();
    for (TrainLabelSentenceInfoPO labelSentenceInfoPO : labelSentenceInfoPOS) {
      TextTrainLabelDiffVO vo = new TextTrainLabelDiffVO();
      vo.setSentence(labelSentenceInfoPO.getSentence());
      vo.setActual(labelSentenceInfoPO.getLabelActual());
      vo.setPredict(labelSentenceInfoPO.getLabelPredict());
      vos.add(vo);
    }
    return new PageResultVo<>(
        "success",
        0,
        true,
        vos,
        poPageVO.getCurPage(),
        poPageVO.getPageSize(),
        poPageVO.getTotalRows(),
        poPageVO.getPageCount());
  }

  @Override
  public ResultVo<String> downloadAnalysisResult(NerTrainLabelReq req) {
    TrainRecordsPO trainRecord = trainRecordsService.getById(req.getRecordId());
    if (trainRecord == null) {
      return ResultVo.create(NOT_EXIST, false, null);
    }
    String fileName =
        fileUploadProperties.getOssPrefix() + trainRecord.getTaskId() + "/report/" + trainRecord.getId() + ".xlsx";
    boolean exit = ossService.fileExit(fileName);
    if (exit) {
      return ResultVo.createSuccess(ossService.getUrlSigned(fileName, 1800 * 1000));
    }
    // ??????????????????????????????
    // ????????????????????????
    List<TrainLabelSentenceInfoPO> labelSentenceInfoPOS = trainLabelSentenceInfoService.selectAllByTrainRecordId(
        req.getRecordId());
    Stream<PredictDetail> streamA = labelSentenceInfoPOS.stream().map(this::convertForTrainSentence);
    List<PredictDetail> result = streamA.sorted(Comparator.comparingLong(PredictDetail::getId))
        .collect(Collectors.toList());
    List<NerTrainLabelResultPO> labelResultPOS = nerTrainLabelResultService.selectByTrainRecordId(
        trainRecord.getId());
    List<LabelResultDetail> labelList = labelResultPOS.stream().map(this::convertForLabel).collect(Collectors.toList());
    Path path = FileMethods.prepareFile(fileUploadProperties.getTempPath(), NanoId.randomNanoId() + ".xlsx");
    try {
      try (ExcelWriter excelWriter = EasyExcel.write(path.toFile()).build()) {
        // ???????????????
        WriteSheet dataSheet = EasyExcel.writerSheet("????????????").head(PredictDetail.class).build();
        excelWriter.write(result, dataSheet);
        // ???????????????
        WriteSheet labelSheet = EasyExcel.writerSheet("????????????").head(LabelResultDetail.class).build();
        excelWriter.write(labelList, labelSheet);
      }
      // ????????????
      FileUpload fileUpload = new FileUpload();
      fileUpload.setFile(path.toFile());
      fileUpload.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
      fileUpload.setDestPath(fileName);
      Map<String, String> tag = new HashMap<>();
      tag.put(CommonConstant.OSS_TEMP_TAG, "30");
      fileUpload.setTagMap(tag);
      ossService.uploadFiles(Collections.singletonList(fileUpload));
    } finally {
      try {
        Files.delete(path);
      } catch (IOException e) {
        log.error("[ModelTrainServiceImpl.downloadAnalysisResult] ?????????????????????????????????file:{}", path);
      }
    }
    return ResultVo.createSuccess(ossService.getUrlSigned(fileName, 1800 * 1000));
  }

  @Override
  public ResultVo<MatrixVO> getConfusionMatrix(NerTrainLabelReq req) {
    TrainResultWithRecordPO recordPO = trainResultService.selectMatrixByTrainId(req.getTaskId(),
        req.getRecordId());
    MatrixVO matrixVO = new MatrixVO();
    matrixVO.setMatrix(new ArrayList<>());
    matrixVO.setLabels(new ArrayList<>());
    if (recordPO != null) {
      try {
        if (StringUtils.isNotBlank(recordPO.getConfusionMx())) {
          List<List<Integer>> matrix = objectMapper.readValue(recordPO.getConfusionMx(),
              new TypeReference<>() {
              });
          matrixVO.setMatrix(matrix);
        }
        if (StringUtils.isNotBlank(recordPO.getLabelArray())) {
          List<String> labels = objectMapper.readValue(recordPO.getLabelArray(), new TypeReference<>() {
          });
          matrixVO.setLabels(labels);
        }
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("????????????????????????", e);
      }

    }
    return ResultVo.createSuccess(matrixVO);
  }

  @Override
  public PageResultVo<List<TextTrainLabelDiffVO>> getMatrixDetail(MatrixDetailReq req) {
    Page<TrainLabelSentenceInfoPO> page = Page.of(req.getCurPage(), req.getPageSize());
    TrainLabelSentenceInfoPO po = new TrainLabelSentenceInfoPO();
    po.setTrainRecordId(req.getRecordId());
    po.setLabelActual(req.getActual());
    po.setLabelPredict(req.getPredict());
    PageVO<TrainLabelSentenceInfoPO> pageVO = trainLabelSentenceInfoService.pageByLabel(page, po);
    return PageResultVo.createSuccess(pageVO.convert(p -> {
      TextTrainLabelDiffVO vo = new TextTrainLabelDiffVO();
      vo.setSentence(p.getSentence());
      vo.setActual(p.getLabelActual());
      vo.setPredict(p.getLabelPredict());
      return vo;
    }));
  }

  private PredictDetail convertForTrainSentence(TrainLabelSentenceInfoPO po) {
    PredictDetail detail = new PredictDetail();
    detail.setId(po.getDataId());
    detail.setSentence(po.getSentence());
    detail.setActual(po.getLabelActual());
    detail.setPredict(po.getLabelPredict());
    return detail;
  }

  private PredictDetail convertForTestData(TestDataPO po) {
    PredictDetail detail = new PredictDetail();
    detail.setId(po.getDataId());
    detail.setSentence(po.getSentence());
    detail.setActual(po.getLabelDes());
    detail.setPredict(po.getLabelDes());
    return detail;
  }

  private LabelResultDetail convertForLabel(NerTrainLabelResultPO po) {
    LabelResultDetail detail = new LabelResultDetail();
    detail.setLabelDes(po.getLabelDes());
    detail.setSample(po.getSamples());
    detail.setPrecision(po.getTrainPrecision());
    detail.setRecall(po.getRecall());
    detail.setErrorCount(po.getErrorCount());
    return detail;
  }

  private String commitTrainTask(TrainReq req, IntegrationRecordsPO lastIntegrationRecord, LocalDateTime createTime) {
    long taskId = req.getTaskId();
    TrainRecordsPO po = new TrainRecordsPO();
    po.setTrainStatus(0);
    po.setTaskId(taskId);
    po.setDatasetId(lastIntegrationRecord.getDatasetId());
    po.setModelType(req.getModel());
    // ????????????
    long labelCount = labelInfoService.countLabelInfoByTaskId(taskId);
    po.setLabelCount((int) labelCount);
    // ????????????
    Long ruleCount = ruleInfoService.countRuleComplete(taskId);
    po.setRuleCount(ruleCount.intValue());
    // ???????????????
    long count = integrateLabelResultService.countCorrectByTaskId(taskId);
    long trainCount = testDataService.countByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
    po.setTrainCount((int) count + (int) trainCount);
    trainRecordsService.save(po);
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setType(AlgorithmTaskType.MODEL_TRAIN);
    algorithmTask.setRecordId(po.getId());
    Map<String, Object> params = new HashMap<>();
    params.put("model", req.getModel());
    params.put("testLabel", lastIntegrationRecord.getResultFileAddress());
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    params.put(SESSION_UID, userInfo.getUid());
    params.put(UPDATE_TIME, createTime);
    algorithmTask.setParams(params);
    algoTaskAppendEventPublisher.publish(algorithmTask);
    return po.getId().toString();
  }

  private TrainResultVO convert(TrainResultWithRecordPO po) {
    TrainResultVO vo = new TrainResultVO();
    BeanCopyUtil.copy(po, vo);
    vo.setModelFileAddress(po.getFileAddress());
    vo.setTrainFileAddress(po.getTrainFile());
    Integer modelType = po.getModelType();
    if (modelType != null) {
      String name = null;
      try {
        name = ModelType.getFromType(modelType).getName();
      } catch (Exception e) {
        log.error("[ModelTrainServiceImpl.convert]", e);
      }
      vo.setModelType(name);
    }
    return vo;
  }

  @Autowired
  public void setTrainResultService(TrainResultService trainResultService) {
    this.trainResultService = trainResultService;
  }

  @Autowired
  public void setTrainRecordsService(TrainRecordsService trainRecordsService) {
    this.trainRecordsService = trainRecordsService;
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setAlgoTaskAppendEventPublisher(
      AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher) {
    this.algoTaskAppendEventPublisher = algoTaskAppendEventPublisher;
  }

  @Autowired
  public void setRuleOverviewService(RuleOverviewService ruleOverviewService) {
    this.ruleOverviewService = ruleOverviewService;
  }

  @Autowired
  public void setIntegrateLabelResultService(
      IntegrateLabelResultService integrateLabelResultService) {
    this.integrateLabelResultService = integrateLabelResultService;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setTrainResultCallbackListener(
      TrainResultCallbackListener trainResultCallbackListener) {
    this.trainResultCallbackListener = trainResultCallbackListener;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setCommonProperties(CommonProperties commonProperties) {
    this.commonProperties = commonProperties;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setNerTrainLabelResultService(NerTrainLabelResultService nerTrainLabelResultService) {
    this.nerTrainLabelResultService = nerTrainLabelResultService;
  }

  @Autowired
  public void setTrainLabelSentenceInfoService(
      TrainLabelSentenceInfoService trainLabelSentenceInfoService) {
    this.trainLabelSentenceInfoService = trainLabelSentenceInfoService;
  }

  @Autowired
  public void setNerTrainLabelDetailService(NerTrainLabelDetailService nerTrainLabelDetailService) {
    this.nerTrainLabelDetailService = nerTrainLabelDetailService;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
