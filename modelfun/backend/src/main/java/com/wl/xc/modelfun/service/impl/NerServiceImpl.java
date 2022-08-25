package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.AUTO_LABEL_FAILED;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.AUTO_LABEL_RUNNING;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.NOT_AUTO_LABELED;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.TRAIN_RUNNING;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.AutoLabelType;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.dto.NerAutoLabelCallbackDTO;
import com.wl.xc.modelfun.entities.dto.NerAutoLabelCallbackDTO.ResultDTO;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO.Arg;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO.UnlabelResDTO;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO.UnlabelResDTO.EntitiesDTO;
import com.wl.xc.modelfun.entities.dto.TrainCallbackDTO;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.model.NerTestDataModel;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.NerAutoLabelMapPO;
import com.wl.xc.modelfun.entities.po.NerAutoLabelResultPO;
import com.wl.xc.modelfun.entities.po.NerAutoLabelTrainPO;
import com.wl.xc.modelfun.entities.po.NerDataLabelPO;
import com.wl.xc.modelfun.entities.po.NerDataLabelWithDesPO;
import com.wl.xc.modelfun.entities.po.NerTestDataPO;
import com.wl.xc.modelfun.entities.po.NerTrainLabelDetailPO;
import com.wl.xc.modelfun.entities.po.NerTrainLabelResultPO;
import com.wl.xc.modelfun.entities.po.TrainLabelSentenceInfoPO;
import com.wl.xc.modelfun.entities.po.TrainRecordsPO;
import com.wl.xc.modelfun.entities.po.TrainResultPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.entities.req.DatasetDetailReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelPageReq;
import com.wl.xc.modelfun.entities.req.NerTrainLabelReq;
import com.wl.xc.modelfun.entities.vo.NerDataLabelDataVO;
import com.wl.xc.modelfun.entities.vo.NerDataLabelDataVO.LabelsDTO;
import com.wl.xc.modelfun.entities.vo.NerTrainLabelDiffVO;
import com.wl.xc.modelfun.entities.vo.NerTrainLabelResultVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.ModelTrainService;
import com.wl.xc.modelfun.service.NerAutoLabelMapService;
import com.wl.xc.modelfun.service.NerAutoLabelResultService;
import com.wl.xc.modelfun.service.NerAutoLabelTrainService;
import com.wl.xc.modelfun.service.NerDataLabelService;
import com.wl.xc.modelfun.service.NerService;
import com.wl.xc.modelfun.service.NerTestDataService;
import com.wl.xc.modelfun.service.NerTrainLabelDetailService;
import com.wl.xc.modelfun.service.NerTrainLabelResultService;
import com.wl.xc.modelfun.service.TrainLabelSentenceInfoService;
import com.wl.xc.modelfun.service.TrainRecordsService;
import com.wl.xc.modelfun.service.TrainResultService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.algorithm.AlgoTaskAppendEventPublisher;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.daemon.TrainResultCallbackListener;
import com.wl.xc.modelfun.utils.BeanCopyUtil;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/5/25 14:06
 */
@Slf4j
@Service
public class NerServiceImpl implements NerService {

  private NerTestDataService nerTestDataService;

  private NerDataLabelService nerDataLabelService;

  private NerAutoLabelResultService nerAutoLabelResultService;

  private NerAutoLabelMapService autoLabelMapService;

  private UnlabelDataService unlabelDataService;

  private TrainResultCallbackListener trainResultCallbackListener;

  private ModelTrainService modelTrainService;

  private TrainRecordsService trainRecordsService;

  private TrainResultService trainResultService;

  private LabelInfoService labelInfoService;

  private NerTrainLabelResultService nerTrainLabelResultService;

  private TrainLabelSentenceInfoService trainLabelSentenceInfoService;

  private NerTrainLabelDetailService nerTrainLabelDetailService;

  private ObjectMapper objectMapper;

  private NerService nerService;

  private IntegrationRecordsService integrationRecordsService;

  private DatasetInfoService datasetInfoService;

  private AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher;

  private NerAutoLabelTrainService nerAutoLabelTrainService;

  @Override
  public PageVO<NerDataLabelDataVO> pageNerTestData(DatasetDetailReq req) {
    return getTestOrTrainPage(req, DatasetType.TEST);
  }

  @Override
  public PageVO<NerDataLabelDataVO> pageNerLabelResult(DatasetDetailReq req) {
    AutoLabelType type = AutoLabelType.getFromType(req.getDataType());
    return getCorrectOrErrorPage(req, type);
  }

  @Override
  public PageVO<NerDataLabelDataVO> pageNerTrainData(DatasetDetailReq req) {
    return getTestOrTrainPage(req, DatasetType.TRAIN);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> updateNerDataLabel(NerDataLabelDataVO req, Long taskId) {
    Long dataId = req.getDataId();
    // 先删除原先的标签
    nerDataLabelService.deleteBySentenceId(taskId, dataId, req.getDataType());
    List<LabelsDTO> labels = req.getLabels();
    if (labels == null || labels.isEmpty()) {
      return ResultVo.createSuccess(true);
    }
    List<NerDataLabelPO> list = new ArrayList<>(labels.size());
    for (LabelsDTO label : labels) {
      if (label.getLabelId() == null) {
        return ResultVo.create("标签id不能为空", -1, false, false);
      }
      if (label.getStartOffset() == null || label.getEndOffset() == null) {
        return ResultVo.create("标签位置不能为空", -1, false, false);
      }
      NerDataLabelPO po = new NerDataLabelPO();
      BeanCopyUtil.copy(label, po);
      po.setTaskId(taskId);
      po.setSentenceId(dataId);
      po.setDataType(req.getDataType());
      list.add(po);
    }
    nerDataLabelService.saveForBatchNoLog(list);
    return ResultVo.createSuccess(true);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> updateNerAutoLabelData(NerDataLabelDataVO req, Long taskId) {
    Long dataId = req.getDataId();
    // 先删除原先的标签
    autoLabelMapService.deleteBySentenceId(taskId, dataId, req.getDataType());
    List<LabelsDTO> labels = req.getLabels();
    if (labels == null || labels.isEmpty()) {
      return ResultVo.createSuccess(true);
    }
    List<NerAutoLabelResultPO> list = new ArrayList<>(labels.size());
    for (LabelsDTO label : labels) {
      if (label.getLabelId() == null) {
        return ResultVo.create("标签id不能为空", -1, false, false);
      }
      if (label.getStartOffset() == null || label.getEndOffset() == null) {
        return ResultVo.create("标签位置不能为空", -1, false, false);
      }
      NerAutoLabelResultPO po = new NerAutoLabelResultPO();
      BeanCopyUtil.copy(label, po);
      po.setTaskId(taskId);
      po.setSentenceId(dataId);
      po.setDataType(req.getDataType());
      list.add(po);
    }
    nerAutoLabelResultService.saveForBatchNoLog(list);
    return ResultVo.createSuccess(true);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> importAutoLabelData(NerDataLabelDataVO dataLabel, Long taskId) {
    Long maxSentenceId = nerTestDataService.getMaxSentenceId(taskId, DatasetType.TRAIN.getType());
    maxSentenceId = maxSentenceId == null ? 1 : maxSentenceId + 1;
    NerTestDataPO testDataPO = new NerTestDataPO();
    testDataPO.setTaskId(taskId);
    testDataPO.setDataId(maxSentenceId);
    testDataPO.setSentence(dataLabel.getSentence());
    testDataPO.setShowData(1);
    testDataPO.setDataType(DatasetType.TRAIN.getType());
    List<NerDataLabelPO> nerDataLabelPOS = new ArrayList<>(dataLabel.getLabels().size());
    for (LabelsDTO labelDTO : dataLabel.getLabels()) {
      NerDataLabelPO po = new NerDataLabelPO();
      BeanCopyUtil.copy(labelDTO, po);
      po.setTaskId(taskId);
      po.setSentenceId(maxSentenceId);
      po.setDataType(DatasetType.TRAIN.getType());
      nerDataLabelPOS.add(po);
    }
    // 插入到训练集
    nerTestDataService.save(testDataPO);
    nerDataLabelService.saveForBatchNoLog(nerDataLabelPOS);
    // 删除自动标注的数据
    nerAutoLabelResultService.removeById(dataLabel.getId());
    autoLabelMapService.deleteBySentenceId(taskId, dataLabel.getDataId(), dataLabel.getDataType());
    // 从未标注集中删除数据
    unlabelDataService.deleteByTaskAndDataId(taskId, dataLabel.getDataId());
    return ResultVo.createSuccess(true);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> saveTrainAsync(NerTrainCallbackDTO dto) {
    trainResultCallbackListener.removeCallbackListener(dto.getRecordId());
    if (Boolean.FALSE.equals(dto.getState())) {
      TrainCallbackDTO trainCallbackDTO = new TrainCallbackDTO();
      trainCallbackDTO.setTaskId(dto.getTaskId());
      trainCallbackDTO.setRecordId(dto.getRecordId());
      modelTrainService.saveFailedTrainResult(trainCallbackDTO);
      return ResultVo.createSuccess(false);
    }
    TrainRecordsPO trainRecordsPO = trainRecordsService.getById(dto.getRecordId());
    TrainResultPO trainResultPO = new TrainResultPO();
    trainResultPO.setTaskId(dto.getTaskId());
    trainResultPO.setTrainRecordId(dto.getRecordId());
    // 准确率，算法返回
    Double a = dto.getResults().getAccuracy();
    trainResultPO.setAccuracy(CalcUtil.multiply(a == null ? "0" : a.toString(), "100"));
    // 精确率，算法返回
    a = dto.getResults().getPrecision();
    trainResultPO.setTrainPrecision(CalcUtil.multiply(a == null ? "0" : a.toString(), "100"));
    // 召回率，算法返回
    a = dto.getResults().getRecall();
    trainResultPO.setRecall(CalcUtil.multiply(a == null ? "0" : a.toString(), "100"));
    // F1 值，算法返回
    a = dto.getResults().getFscore();
    trainResultPO.setF1Score(CalcUtil.multiply(a == null ? "0" : a.toString(), "100"));
    // oss文件地址，算法返回
    // 标签类别
    trainResultPO.setLabelTypeCount(trainRecordsPO.getLabelCount());
    // 规则数量
    trainResultPO.setRuleCount(trainRecordsPO.getRuleCount());
    // 训练集数量
    trainResultPO.setTrainCount(trainRecordsPO.getTrainCount());
    // 模型地址
    trainResultPO.setFileAddress(dto.getResults().getModelPath());
    trainResultService.save(trainResultPO);
    saveTrainResult(trainResultPO, dto);
    log.info("[ModelTrainServiceImpl.saveTrainResultAsync] 训练结果入库完成");
    return ResultVo.createSuccess(true);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> saveAutoAsync(NerAutoLabelCallbackDTO dto) {
    IntegrationRecordsPO recordsPO = integrationRecordsService.getById(dto.getRecordId());
    if (recordsPO == null) {
      dto.setDetail("服务器内部错误，记录不存在");
      return ResultVo.createSuccess(false);
    }
    if (Boolean.FALSE.equals(dto.getState())) {
      IntegrationRecordsPO p = new IntegrationRecordsPO();
      p.setLabeled(3);
      p.setId(dto.getRecordId());
      p.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsService.updateById(p);
      return ResultVo.createSuccess(false);
    }
    // 高置信数据ID，从0开始
    try {
      ResultDTO results = dto.getResults();
      List<Integer> certaintyIdx = results.getCertaintyIdx();
      // 待审核数据ID，从0开始
      List<Integer> uncertaintyIdx = results.getUncertaintyIdx();
      List<NerTestDataModel> unlabelRes = results.getUnlabelRes();
      List<UnlabelDataPO> allUnlabelData = unlabelDataService.getAllByTaskId(dto.getTaskId());
      List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(dto.getTaskId());
      Map<String, Integer> labelMap = labelInfoPOS.stream()
          .collect(Collectors.toMap(LabelInfoPO::getLabelDesc, LabelInfoPO::getLabelId));
      List<NerAutoLabelResultPO> resultList = new ArrayList<>(unlabelRes.size());
      List<NerAutoLabelMapPO> labelMapList = new ArrayList<>(unlabelRes.size());
      int labeledCount = 0;
      for (Integer idx : certaintyIdx) {
        NerTestDataModel model = unlabelRes.get(idx);
        UnlabelDataPO unlabelDataPO = allUnlabelData.get(idx);
        NerAutoLabelResultPO autoLabelResultPO = new NerAutoLabelResultPO();
        autoLabelResultPO.setSentence(unlabelDataPO.getSentence());
        autoLabelResultPO.setSentenceId(unlabelDataPO.getDataId());
        autoLabelResultPO.setTaskId(dto.getTaskId());
        autoLabelResultPO.setDataType(AutoLabelType.CORRECT.getType());
        resultList.add(autoLabelResultPO);
        List<NerTestDataModel.EntitiesDTO> entities = model.getEntities();
        if (entities == null || entities.isEmpty()) {
          continue;
        }
        labeledCount++;
        for (NerTestDataModel.EntitiesDTO entity : entities) {
          if (!labelMap.containsKey(entity.getLabel())) {
            continue;
          }
          NerAutoLabelMapPO mapPO = new NerAutoLabelMapPO();
          mapPO.setTaskId(dto.getTaskId());
          mapPO.setSentenceId(unlabelDataPO.getDataId());
          mapPO.setLabelId(labelMap.get(entity.getLabel()));
          mapPO.setDataType(AutoLabelType.CORRECT.getType());
          mapPO.setStartOffset(entity.getStartOffset());
          mapPO.setEndOffset(entity.getEndOffset());
          mapPO.setDataId(entity.getId() == null ? null : entity.getId().longValue());
          labelMapList.add(mapPO);
        }
      }
      for (Integer idx : uncertaintyIdx) {
        NerTestDataModel model = unlabelRes.get(idx);
        UnlabelDataPO unlabelDataPO = allUnlabelData.get(idx);
        NerAutoLabelResultPO autoLabelResultPO = new NerAutoLabelResultPO();
        autoLabelResultPO.setSentence(unlabelDataPO.getSentence());
        autoLabelResultPO.setSentenceId(unlabelDataPO.getDataId());
        autoLabelResultPO.setTaskId(dto.getTaskId());
        autoLabelResultPO.setDataType(AutoLabelType.DOUBTFUL.getType());
        resultList.add(autoLabelResultPO);
        List<NerTestDataModel.EntitiesDTO> entities = model.getEntities();
        if (entities == null || entities.isEmpty()) {
          continue;
        }
        labeledCount++;
        for (NerTestDataModel.EntitiesDTO entity : entities) {
          if (!labelMap.containsKey(entity.getLabel())) {
            continue;
          }
          NerAutoLabelMapPO mapPO = new NerAutoLabelMapPO();
          mapPO.setLabelId(labelMap.get(entity.getLabel()));
          mapPO.setTaskId(dto.getTaskId());
          mapPO.setSentenceId(unlabelDataPO.getDataId());
          mapPO.setDataType(AutoLabelType.DOUBTFUL.getType());
          mapPO.setStartOffset(entity.getStartOffset());
          mapPO.setEndOffset(entity.getEndOffset());
          mapPO.setDataId(entity.getId() == null ? null : entity.getId().longValue());
          labelMapList.add(mapPO);
        }
      }
      List<Integer> trainIdx = results.getTrainIdx();
      if (trainIdx != null && !trainIdx.isEmpty()) {
        List<NerAutoLabelTrainPO> trainList = new ArrayList<>(trainIdx.size());
        for (Integer idx : trainIdx) {
          NerAutoLabelTrainPO trainPO = new NerAutoLabelTrainPO();
          trainPO.setTaskId(dto.getTaskId());
          trainPO.setDataId(allUnlabelData.get(idx).getDataId());
          trainList.add(trainPO);
        }
        nerAutoLabelTrainService.saveBatch(trainList);
      }
      // 入库
      if (!resultList.isEmpty()) {
        nerAutoLabelResultService.saveForBatchNoLog(resultList);
      }
      if (!labelMapList.isEmpty()) {
        autoLabelMapService.saveForBatchNoLog(labelMapList);
      }
      recordsPO.setLabeled(2);
      recordsPO.setTestF1Score(CalcUtil.multiply(results.getFscore() + "", "100", 2));
      // 这里把精准率的结果存到准确率的字段中，因为不想再多加字段
      recordsPO.setTestAccuracy(CalcUtil.multiply(results.getPrecision() + "", "100", 2));
      recordsPO.setTestRecall(CalcUtil.multiply(results.getRecall() + "", "100", 2));
      int size = allUnlabelData.size();
      if (size > 0) {
        recordsPO.setUnlabelCoverage(CalcUtil
            .multiply(CalcUtil.divide(labeledCount, size, 2), "100", 2));
      } else {
        recordsPO.setUnlabelCoverage("0");
      }
      recordsPO.setTrainSentenceCount((long) (certaintyIdx.size() + uncertaintyIdx.size()));
      LocalDateTime updateDatetime = recordsPO.getUpdateDatetime();
      LocalDateTime now = LocalDateTime.now();
      recordsPO.setTimeCost((int) updateDatetime.until(now, ChronoUnit.SECONDS));
      recordsPO.setUpdateDatetime(now);
      integrationRecordsService.updateById(recordsPO);
      return ResultVo.createSuccess(true);
    } catch (Exception e) {
      log.error("[NerServiceImpl.saveAutoAsync]", e);
      throw new BusinessIllegalStateException("服务器内部错误");
    }
  }

  @Override
  public ResultVo<Boolean> nerTrain(Long taskId) {
    DatasetInfoPO datasetInfo = datasetInfoService.getLastDatasetInfo(taskId);
    if (datasetInfo == null) {
      return ResultVo.create("请先上传数据集文件", -1, false, null);
    }
    // 判断是否有未标注完的数据存在训练集或者测试集中
    int unLabeledData =
        nerTestDataService.countUnLabeledData(taskId, DatasetType.TRAIN.getType());
    if (unLabeledData > 0) {
      return ResultVo.create("当前有未标注完的数据存在测试集中，请先标注完再进行自动标注", -1, false, null);
    }
    unLabeledData =
        nerTestDataService.countUnLabeledData(taskId, DatasetType.TEST.getType());
    if (unLabeledData > 0) {
      return ResultVo.create("当前有未标注完的数据存在训练集中，请先标注完再进行自动标注", -1, false, null);
    }
    // 检测自动标注情况
    // 获取最近一次集成记录，如果没有集成记录，或者集成尚未完成，不允许训练
    IntegrationRecordsPO lastIntegrationRecord = integrationRecordsService.getLastIntegrationRecord(taskId);
    if (lastIntegrationRecord == null) {
      return ResultVo.create(NOT_AUTO_LABELED, false, null);
    }
    // 正在自动标注中
    if (lastIntegrationRecord.getLabeled() == 1) {
      return ResultVo.create(AUTO_LABEL_RUNNING, false, null);
    }
    // 判断自动标注结果，如果自动标注是失败的，不允许训练
    if (lastIntegrationRecord.getLabeled() == 3) {
      return ResultVo.create(AUTO_LABEL_FAILED, false, null);
    }
    TrainRecordsPO lastTrainRecord = trainRecordsService.getLastTrainRecord(taskId);
    if (lastTrainRecord == null) {
      // 不存在训练记录，直接开始训练
      commitTrainTask(taskId, datasetInfo.getId());
      return ResultVo.createSuccess(true);
    }
    if (lastTrainRecord.getTrainStatus() == 0) {
      // 训练中
      return ResultVo.create(TRAIN_RUNNING, false, null);
    }
    commitTrainTask(taskId, datasetInfo.getId());
    return ResultVo.createSuccess(true);
  }

  @Override
  public ResultVo<List<NerTrainLabelResultVO>> getTrainLabelInfo(NerTrainLabelReq req) {
    List<NerTrainLabelResultPO> labelResultPOS = nerTrainLabelResultService.selectByTrainRecordId(
        req.getRecordId());
    List<NerTrainLabelResultVO> list = labelResultPOS.stream().map(po -> {
      NerTrainLabelResultVO vo = new NerTrainLabelResultVO();
      BeanCopyUtil.copy(po, vo);
      return vo;
    }).collect(Collectors.toList());
    return ResultVo.createSuccess(list);
  }

  @Override
  public PageResultVo<List<NerTrainLabelDiffVO>> getTrainLabelInfoDiff(NerTrainLabelPageReq req) {
    Page<NerTrainLabelDetailPO> page = Page.of(req.getCurPage(), req.getPageSize());
    PageVO<NerTrainLabelDetailPO> poPageVO = nerTrainLabelDetailService.pageByTrainLabelId(page,
        req.getRecordId());
    List<NerTrainLabelDetailPO> detailPOS = poPageVO.getRecords();
    if (detailPOS == null || detailPOS.isEmpty()) {
      return PageResultVo.createSuccess(poPageVO.convert(p -> new NerTrainLabelDiffVO()));
    }
    List<Long> collect = detailPOS.stream().map(NerTrainLabelDetailPO::getDataId).collect(Collectors.toList());
    List<TrainLabelSentenceInfoPO> labelSentenceInfoPOS = trainLabelSentenceInfoService.selectByTrainRecordId(
        req.getTrainRecordId(), collect);
    List<NerTrainLabelDiffVO> vos = new ArrayList<>();
    for (TrainLabelSentenceInfoPO labelSentenceInfoPO : labelSentenceInfoPOS) {
      NerTrainLabelDiffVO vo = new NerTrainLabelDiffVO();
      vo.setSentence(labelSentenceInfoPO.getSentence());
      String actual = labelSentenceInfoPO.getLabelActual();
      try {
        List<NerTestDataModel.EntitiesDTO> actualEntities = objectMapper.readValue(actual, new TypeReference<>() {
        });
        vo.setActual(actualEntities);
        String predict = labelSentenceInfoPO.getLabelPredict();
        List<NerTestDataModel.EntitiesDTO> predictEntities = objectMapper.readValue(predict, new TypeReference<>() {
        });
        vo.setPredict(predictEntities);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("服务器内部错误", e);
      }
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
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> delNerAutoLabelData(NerDataLabelDataVO req, Long taskId) {
    NerAutoLabelResultPO resultPO = nerAutoLabelResultService.getById(req.getId());
    if (resultPO == null) {
      return ResultVo.create("未找到对应的自动标注结果", -1, false, null);
    }
    // 删除自动标注的数据
    nerAutoLabelResultService.removeById(resultPO.getId());
    autoLabelMapService.deleteBySentenceId(taskId, resultPO.getSentenceId(), resultPO.getDataType());
    return ResultVo.createSuccess(true);
  }

  private void commitTrainTask(Long taskId, Integer id) {
    TrainRecordsPO po = new TrainRecordsPO();
    po.setTrainStatus(0);
    po.setTaskId(taskId);
    po.setDatasetId(id);
    // 标签类别
    long labelCount = labelInfoService.countLabelInfoByTaskId(taskId);
    po.setLabelCount((int) labelCount);
    // 训练集数量
    long trainCount = nerTestDataService.countByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
    long highCount = nerAutoLabelResultService.countByTaskIdAndType(taskId, AutoLabelType.CORRECT.getType());
    po.setTrainCount((int) (trainCount + highCount));
    trainRecordsService.save(po);
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setType(AlgorithmTaskType.NER_TRAIN);
    algorithmTask.setRecordId(po.getId());
    Map<String, Object> params = new HashMap<>();
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    params.put(SESSION_UID, userInfo.getUid());
    algorithmTask.setParams(params);
    algoTaskAppendEventPublisher.publish(algorithmTask);
  }

  private void saveTrainResult(TrainResultPO po, NerTrainCallbackDTO dto) {
    Map<String, Arg> report = dto.getResults().getReport();
    List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(po.getTaskId());
    List<String> collect = labelInfoPOS.stream().map(LabelInfoPO::getLabelDesc).collect(Collectors.toList());
    List<NerTrainLabelResultPO> nerTrainLabelResultPOs = new ArrayList<>();
    for (String s : collect) {
      Arg arg = report.get(s);
      if (arg == null) {
        continue;
      }
      NerTrainLabelResultPO resultPO = new NerTrainLabelResultPO();
      resultPO.setTrainPrecision(CalcUtil.multiply(arg.getPrecision() + "", "100"));
      resultPO.setRecall(CalcUtil.multiply(arg.getRecall() + "", "100"));
      resultPO.setLabelDes(s);
      resultPO.setSamples(arg.getSupport());
      String multiply = CalcUtil.multiply(arg.getPrecision() + "", arg.getSupport() + "", 0, false);
      resultPO.setErrorCount(Math.max(arg.getSupport() - Integer.parseInt(multiply), 0));
      resultPO.setTrainRecordId(po.getTrainRecordId());
      nerTrainLabelResultPOs.add(resultPO);
    }
    // 入库标签结果
    nerTrainLabelResultService.saveBatchNoLog(nerTrainLabelResultPOs);
    long count = nerTestDataService.countByTaskIdAndType(po.getTaskId(), DatasetType.TEST.getType());
    DatasetDetailReq req = new DatasetDetailReq();
    req.setTaskId(po.getTaskId());
    req.setCurPage(1L);
    req.setPageSize(count);
    PageVO<NerDataLabelDataVO> pageVO = nerService.pageNerTestData(req);
    List<NerDataLabelDataVO> testDataRecords = pageVO.getRecords();
    Map<Integer, List<EntitiesDTO>> sentenceLabels = getSentenceLabels(testDataRecords);
    Map<Long, String> sentenceIdMap = testDataRecords.stream()
        .collect(Collectors.toMap(NerDataLabelDataVO::getDataId, NerDataLabelDataVO::getSentence));
    List<UnlabelResDTO> unlabelRes = dto.getResults().getTestRes();
    for (int i = 0; i < unlabelRes.size(); i++) {
      UnlabelResDTO unlabelRe = unlabelRes.get(i);
      unlabelRe.setId(i);
    }
    // 错误的语料集合
    List<TrainLabelSentenceInfoPO> labelSentenceInfoPOS = new ArrayList<>();
    for (int i = 0, unlabelResSize = testDataRecords.size(); i < unlabelResSize; i++) {
      UnlabelResDTO unlabelRe = unlabelRes.get(i);
      // 测试集语料对应标签
      List<EntitiesDTO> testEntities = sentenceLabels.get(i);
      // 算法返回语料对应标签
      List<EntitiesDTO> trainEntities = unlabelRe.getEntities();
      // 遍历测试集的语料的标签
      boolean allCorrect = true;
      if (trainEntities == null || trainEntities.isEmpty()) {
        allCorrect = false;
      } else {
        for (EntitiesDTO testEntity : testEntities) {
          boolean correct = false;
          for (EntitiesDTO trainEntity : trainEntities) {
            // 只要有一个标签正确，就认为该句子的标签正确
            if (trainEntity.getLabel() != null
                && trainEntity.getEndOffset() != null
                && trainEntity.getStartOffset() != null
                && trainEntity.simpleEqual(testEntity)) {
              correct = true;
              break;
            }
          }
          if (!correct) {
            allCorrect = false;
            break;
          }
        }
      }
      if (!allCorrect) {
        TrainLabelSentenceInfoPO sentenceInfoPO = new TrainLabelSentenceInfoPO();
        sentenceInfoPO.setSentence(sentenceIdMap.get((long) i));
        sentenceInfoPO.setTrainRecordId(po.getTrainRecordId());
        sentenceInfoPO.setDataId((long) i);
        try {
          sentenceInfoPO.setLabelActual(objectMapper.writeValueAsString(testEntities));
          sentenceInfoPO.setLabelPredict(objectMapper.writeValueAsString(trainEntities));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
        labelSentenceInfoPOS.add(sentenceInfoPO);
      }
    }
    // 先把语料标注结果对比入库
    trainLabelSentenceInfoService.saveBatchNoLog(labelSentenceInfoPOS);
    Map<String, List<Long>> labelSentenceIdMap = getLabelSentenceIdMap(testDataRecords, unlabelRes);
    List<NerTrainLabelDetailPO> nerTrainLabelDetailPOS = new ArrayList<>();
    for (NerTrainLabelResultPO labelResultPO : nerTrainLabelResultPOs) {
      String labelDes = labelResultPO.getLabelDes();
      List<Long> list = labelSentenceIdMap.get(labelDes);
      if (list == null || list.isEmpty()) {
        continue;
      }
      List<NerTrainLabelDetailPO> inner = new ArrayList<>();
      for (Long dataId : list) {
        NerTrainLabelDetailPO detailPO = new NerTrainLabelDetailPO();
        detailPO.setTrainLabelId(labelResultPO.getId());
        detailPO.setDataId(dataId);
        inner.add(detailPO);
      }
      nerTrainLabelDetailPOS.addAll(inner);
    }
    // 把标签对应的语料入库
    nerTrainLabelDetailService.saveBatchNoLog(nerTrainLabelDetailPOS);
    // 更新训练记录状态
    TrainRecordsPO latestRecord = trainRecordsService.selectLatestDataVersion(po.getTaskId());
    int version = 1;
    if (latestRecord != null) {
      version = Integer.parseInt(latestRecord.getDataVersion()) + 1;
    }
    TrainRecordsPO trainRecordsPO = new TrainRecordsPO();
    trainRecordsPO.setTaskId(po.getTaskId());
    trainRecordsPO.setId(po.getTrainRecordId());
    trainRecordsPO.setTrainStatus(1);
    trainRecordsPO.setDataVersion(String.valueOf(version));
    trainRecordsPO.setUpdateDatetime(LocalDateTime.now());
    trainRecordsService.updateById(trainRecordsPO);
  }

  private Map<Integer, List<EntitiesDTO>> getSentenceLabels(List<NerDataLabelDataVO> vos) {
    Map<Integer, List<EntitiesDTO>> hashMap = new HashMap<>();
    for (NerDataLabelDataVO labelDataVO : vos) {
      List<LabelsDTO> labels = labelDataVO.getLabels();
      List<EntitiesDTO> sentenceLabels = new ArrayList<>();
      for (LabelsDTO label : labels) {
        EntitiesDTO dto = new EntitiesDTO();
        dto.setLabel(label.getLabelDes());
        dto.setStartOffset(label.getStartOffset());
        dto.setEndOffset(label.getEndOffset());
        sentenceLabels.add(dto);
      }
      hashMap.put(labelDataVO.getDataId().intValue(), sentenceLabels);
    }
    return hashMap;
  }

  /**
   * 获取标签对应的错误的语料的ID
   *
   * @param vos        测试集语料
   * @param unlabelRes 算法返回的语料
   * @return 标签对应的错误的语料的ID
   */
  private Map<String, List<Long>> getLabelSentenceIdMap(List<NerDataLabelDataVO> vos, List<UnlabelResDTO> unlabelRes) {
    // 标签对应的语料打标结果
    Map<String, List<Long>> result = new HashMap<>();
    // 标签对应的语料
    Map<String, List<NerDataLabelDataVO>> hashMap = new HashMap<>();
    for (NerDataLabelDataVO labelDataVO : vos) {
      List<LabelsDTO> labels = labelDataVO.getLabels();
      Set<String> labelSet = labels.stream().map(LabelsDTO::getLabelDes).collect(Collectors.toSet());
      for (String label : labelSet) {
        List<NerDataLabelDataVO> list = hashMap.computeIfAbsent(label, k -> new ArrayList<>());
        list.add(labelDataVO);
      }
    }
    int size = unlabelRes.size();
    for (Entry<String, List<NerDataLabelDataVO>> entry : hashMap.entrySet()) {
      List<NerDataLabelDataVO> list = entry.getValue();
      for (NerDataLabelDataVO labelDataVO : list) {
        Long dataId = labelDataVO.getDataId();
        if (dataId >= size) {
          continue;
        }
        UnlabelResDTO resDTO = unlabelRes.get(dataId.intValue());
        List<LabelsDTO> labels = labelDataVO.getLabels();
        // 所有与entry.getKey()相同的标签都正确
        boolean allCorrect = true;
        for (LabelsDTO label : labels) {
          String labelDes = label.getLabelDes();
          // 如果不是当前标签，就跳过
          if (!labelDes.equals(entry.getKey())) {
            continue;
          }
          // 单个标签是否准确
          boolean singleCorrect = false;
          List<EntitiesDTO> trainEntities = resDTO.getEntities();
          if (trainEntities == null || trainEntities.isEmpty()) {
            allCorrect = false;
            break;
          }
          for (EntitiesDTO trainEntity : trainEntities) {
            if (label.getLabelDes().equals(trainEntity.getLabel())
                && label.getStartOffset().equals(trainEntity.getStartOffset())
                && label.getEndOffset().equals(trainEntity.getEndOffset())) {
              singleCorrect = true;
              break;
            }
          }
          // 只要单个标签有一个不正确，就认为该标签对应的整句语料错误
          if (!singleCorrect) {
            allCorrect = false;
            break;
          }
        }
        if (!allCorrect) {
          result.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(dataId);
        }
      }
    }
    return result;
  }

  private PageVO<NerDataLabelDataVO> getTestOrTrainPage(DatasetDetailReq req, DatasetType type) {
    Page<NerTestDataPO> page = Page.of(req.getCurPage(), req.getPageSize());
    NerTestDataPO po = new NerTestDataPO();
    po.setTaskId(req.getTaskId());
    po.setSentence(req.getSentence());
    po.setShowData(1);
    po.setDataType(type.getType());
    // 先分页查询语料内容，然后根据语料ID查询对应的实体列表
    PageVO<NerTestDataPO> nerTestDataPOPageVO = nerTestDataService.pageNerTestData(page, po, req.getLabelId(),
        req.isDesc());
    List<NerTestDataPO> records = nerTestDataPOPageVO.getRecords();
    if (records.size() == 0) {
      return nerTestDataPOPageVO.convert(p -> new NerDataLabelDataVO());
    }
    List<Long> sentenceIds = records.stream().map(NerTestDataPO::getDataId).collect(Collectors.toList());
    List<NerDataLabelWithDesPO> labelList = nerDataLabelService.getBySentenceIdAndType(req.getTaskId(), sentenceIds,
        type.getType());
    // 语料ID和实体列表的映射
    Map<Long, List<NerDataLabelWithDesPO>> map = labelList.stream()
        .collect(Collectors.groupingBy(NerDataLabelWithDesPO::getSentenceId));
    return nerTestDataPOPageVO.convert(p -> convert(p, map.get(p.getDataId())));
  }

  private PageVO<NerDataLabelDataVO> getCorrectOrErrorPage(DatasetDetailReq req, AutoLabelType type) {
    Page<NerAutoLabelResultPO> page = Page.of(req.getCurPage(), req.getPageSize());
    NerAutoLabelResultPO po = new NerAutoLabelResultPO();
    po.setTaskId(req.getTaskId());
    po.setSentence(req.getSentence());
    po.setDataType(type.getType());
    // 先分页查询语料内容，然后根据语料ID查询对应的实体列表
    PageVO<NerAutoLabelResultPO> nerTestDataPOPageVO = nerAutoLabelResultService.pageNerAutoLabelResult(page, po,
        req.getLabelId());
    List<NerAutoLabelResultPO> records = nerTestDataPOPageVO.getRecords();
    if (records.size() == 0) {
      return nerTestDataPOPageVO.convert(p -> new NerDataLabelDataVO());
    }
    List<Long> sentenceIds = records.stream().map(NerAutoLabelResultPO::getSentenceId).collect(Collectors.toList());
    List<NerDataLabelWithDesPO> labelList = autoLabelMapService.getLabelResultByType(req.getTaskId(), sentenceIds,
        type.getType());
    // 语料ID和实体列表的映射
    Map<Long, List<NerDataLabelWithDesPO>> map = labelList.stream()
        .collect(Collectors.groupingBy(NerDataLabelWithDesPO::getSentenceId));
    return nerTestDataPOPageVO.convert(p -> convertAutoLabel(p, map.get(p.getSentenceId())));
  }

  private NerDataLabelDataVO convert(NerTestDataPO po, List<NerDataLabelWithDesPO> labelList) {
    NerDataLabelDataVO vo = new NerDataLabelDataVO();
    vo.setId(po.getId());
    vo.setSentence(po.getSentence());
    vo.setDataId(po.getDataId());
    vo.setDataType(po.getDataType());
    if (labelList == null) {
      vo.setLabels(new ArrayList<>());
      return vo;
    }
    return getNerDataLabelDataVO(labelList, vo);
  }

  private NerDataLabelDataVO getNerDataLabelDataVO(List<NerDataLabelWithDesPO> labelList, NerDataLabelDataVO vo) {
    List<LabelsDTO> collect = labelList.stream().map(p -> {
      LabelsDTO dto = new LabelsDTO();
      BeanCopyUtil.copy(p, dto);
      dto.setLabelDes(p.getLabelDesc());
      return dto;
    }).collect(Collectors.toList());
    vo.setLabels(collect);
    return vo;
  }

  private NerDataLabelDataVO convertAutoLabel(NerAutoLabelResultPO po, List<NerDataLabelWithDesPO> labelList) {
    NerDataLabelDataVO vo = new NerDataLabelDataVO();
    vo.setId(po.getId());
    vo.setDataId(po.getSentenceId());
    vo.setSentence(po.getSentence());
    if (labelList == null) {
      vo.setLabels(new ArrayList<>());
      return vo;
    }
    vo.setDataType(po.getDataType());
    return getNerDataLabelDataVO(labelList, vo);
  }

  @Autowired
  public void setNerTestDataService(NerTestDataService nerTestDataService) {
    this.nerTestDataService = nerTestDataService;
  }

  @Autowired
  public void setNerDataLabelService(NerDataLabelService nerDataLabelService) {
    this.nerDataLabelService = nerDataLabelService;
  }

  @Autowired
  public void setNerAutoLabelResultService(NerAutoLabelResultService nerAutoLabelResultService) {
    this.nerAutoLabelResultService = nerAutoLabelResultService;
  }

  @Autowired
  public void setAutoLabelMapService(NerAutoLabelMapService autoLabelMapService) {
    this.autoLabelMapService = autoLabelMapService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
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

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
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
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setNerService(NerService nerService) {
    this.nerService = nerService;
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setDatasetInfoService(DatasetInfoService datasetInfoService) {
    this.datasetInfoService = datasetInfoService;
  }

  @Autowired
  public void setAlgoTaskAppendEventPublisher(
      AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher) {
    this.algoTaskAppendEventPublisher = algoTaskAppendEventPublisher;
  }

  @Autowired
  public void setTrainResultService(TrainResultService trainResultService) {
    this.trainResultService = trainResultService;
  }

  @Autowired
  public void setNerAutoLabelTrainService(NerAutoLabelTrainService nerAutoLabelTrainService) {
    this.nerAutoLabelTrainService = nerAutoLabelTrainService;
  }
}
