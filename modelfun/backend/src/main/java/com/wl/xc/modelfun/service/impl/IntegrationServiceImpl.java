package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.UPDATE_TIME;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.*;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickCacheKey;

import cn.hutool.core.lang.id.NanoId;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.FileConstant;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.FileMethods;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import com.wl.xc.modelfun.entities.dto.FewShotCallbackDTO;
import com.wl.xc.modelfun.entities.dto.FewShotCallbackDTO.ResultDTO;
import com.wl.xc.modelfun.entities.dto.IntegrateCallbackDTO;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.IntegrateLabelResultPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.IntegrationResultPO;
import com.wl.xc.modelfun.entities.po.IntegrationWithRule;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.RuleResultPO;
import com.wl.xc.modelfun.entities.po.RuleUnlabeledResultPO;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import com.wl.xc.modelfun.entities.req.AutoLabelResultReq;
import com.wl.xc.modelfun.entities.req.IntegrationReq;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.DatasetInfoVO;
import com.wl.xc.modelfun.entities.vo.IntegrateOverviewVO;
import com.wl.xc.modelfun.entities.vo.IntegrationResultVO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.IntegrateLabelResultService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.IntegrationResultService;
import com.wl.xc.modelfun.service.IntegrationService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.NerTestDataService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.algorithm.AlgoTaskAppendEventPublisher;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.file.handlers.text.TextLabelDataModel;
import com.wl.xc.modelfun.utils.BeanCopyUtil;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/4/12 10:55
 */
@Slf4j
@Service
public class IntegrationServiceImpl implements IntegrationService {

  private IntegrationResultService integrationResultService;

  private IntegrationRecordsService integrationRecordsService;

  private DatasetInfoService datasetInfoService;

  private RuleInfoService ruleInfoService;
  private AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher;

  private IntegrateLabelResultService integrateLabelResultService;

  private StringRedisTemplate stringRedisTemplate;

  private ObjectMapper objectMapper;

  private LabelInfoService labelInfoService;

  private UnlabelDataService unlabelDataService;

  private NerTestDataService nerTestDataService;

  private TaskInfoService taskInfoService;

  private TestDataService testDataService;

  private DatasetDetailService datasetDetailService;

  private OssServiceImpl ossService;

  private RuleUnlabeledResultService ruleUnlabeledResultService;

  private FileUploadProperties fileUploadProperties;

  private RuleResultService ruleResultService;

  @Override
  public PageVO<IntegrationResultVO> getIntegrationPage(IntegrationReq req) {
    Page<IntegrationWithRule> page = Page.of(req.getCurPage(), req.getPageSize());
    PageVO<IntegrationWithRule> result = integrationResultService.getIntegrationPage(page, req.getTaskId());
    return result.convert(i -> {
      IntegrationResultVO vo = new IntegrationResultVO();
      BeanCopyUtil.copy(i, vo);
      return vo;
    });
  }

  @Override
  public ResultVo<Long> integrate(TaskIdReq req) {
    // ?????????????????????????????????????????????????????????
    DatasetInfoPO datasetInfo = datasetInfoService.getLastDatasetInfo(req.getTaskId());
    ResultVo<Long> resultVo = checkBeforeIntegrate(req.getTaskId(), datasetInfo);
    if (!resultVo.getSuccess()) {
      return resultVo;
    }
    // ??????????????????
    long count = testDataService.countByTaskIdAndType(req.getTaskId(), DatasetType.TRAIN.getType());
    // ?????????????????????????????????????????????
    IntegrationRecordsPO lastRecord = integrationRecordsService.getLastIntegrationRecord(req.getTaskId());
    if (lastRecord == null) {
      // ????????????????????????????????????????????????
      return count > 0 ? commitFewShot(req.getTaskId(), datasetInfo.getId(), null)
          : commitIntegrationDirect(req.getTaskId(), datasetInfo.getId(), null);
    }
    // ????????????????????????????????????????????????????????????????????????1???????????????????????????????????????
    if (lastRecord.getIntegrateStatus() == 0
        && lastRecord.getUpdateDatetime().isBefore(LocalDateTime.now().minusHours(1))) {
      return ResultVo.create(INTEGRATION_RUNNING, false, null);
    }
    // ??????????????????????????????????????????????????????????????????????????????
    RuleInfoPO rule = ruleInfoService.getRuleByType(req.getTaskId(), RuleType.FEW.getType());
    if (rule != null) {
      // ????????????????????????????????????????????????????????????????????????????????????????????????
      RuleInfoPO lastUpdateRule = ruleInfoService.getLastUpdateRule(req.getTaskId());
      LocalDateTime latestTime = lastUpdateRule.getUpdateDatetime();
      LocalDateTime integrationTime = lastRecord.getUpdateDatetime();
      if (integrationTime.isAfter(latestTime) && integrationTime.isAfter(datasetInfo.getUpdateDatetime())) {
        return ResultVo.create(NOT_MODIFY, false, null);
      }
    }
    return count > 0 ? commitFewShot(req.getTaskId(), datasetInfo.getId(), lastRecord.getCreateDatetime())
        : commitIntegrationDirect(req.getTaskId(), datasetInfo.getId(), lastRecord.getCreateDatetime());
  }

  @Override
  public ResultVo<Long> checkBeforeIntegrate(Long taskId, DatasetInfoPO datasetInfo) {
    if (datasetInfo == null || datasetInfo.getDeleted()) {
      return ResultVo.create(DATASET_NOT_EXIT, false, null);
    }
    // ????????????????????????
    List<RuleInfoPO> ruleInfoList = ruleInfoService.getRuleListByTaskId(taskId);
    if (ruleInfoList.isEmpty()) {
      return ResultVo.create(RULE_NOT_EXIT, false, null);
    }
    if (ruleInfoList.size() < 3) {
      return ResultVo.create(RULE_NOT_ENOUGH, false, null);
    }
    // ????????????????????????????????????
    LocalDateTime latestTime = ruleInfoList.get(0).getUpdateDatetime();
    List<String> runningRuleNames = new ArrayList<>();
    List<String> failedRuleNames = new ArrayList<>();
    for (RuleInfoPO ruleInfo : ruleInfoList) {
      if (ruleInfo.getCompleted() == 0) {
        runningRuleNames.add(ruleInfo.getRuleName());
      } else if (ruleInfo.getCompleted() == 2) {
        failedRuleNames.add(ruleInfo.getRuleName());
      }
      if (ruleInfo.getUpdateDatetime().isAfter(latestTime)) {
        latestTime = ruleInfo.getUpdateDatetime();
      }
    }
    if (!runningRuleNames.isEmpty()) {
      String msg = "?????????" + runningRuleNames + "????????????????????????????????????";
      return ResultVo.create(RULE_RUNNING, msg, false, null);
    }
    if (!failedRuleNames.isEmpty()) {
      String msg = "?????????" + failedRuleNames + "??????????????????????????????";
      return ResultVo.create(RULE_FAILED, msg, false, null);
    }
    // ????????????????????????????????????????????????????????????????????????????????????????????????
    long count = testDataService.countUnLabelTrainData(taskId);
    if (count > 0) {
      return ResultVo.create("?????????????????????????????????????????????????????????", -1, false, null);
    }
    return ResultVo.createSuccess(null);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void saveNewIntegration(IntegrationRecordsPO po) {
    // ??????????????????
    Long taskId = po.getTaskId();
    int i = integrationResultService.deleteByTaskId(taskId);
    log.info("[IntegrationServiceImpl.saveNewIntegration] ???????????????????????????????????????{}???", i);
    // ????????????????????????
    integrationRecordsService.updateById(po);
    // ????????????????????????
    Long recordId = po.getId();
    List<RuleInfoPO> ruleList = ruleInfoService.getRuleListByTaskId(taskId);
    List<IntegrationResultPO> collect =
        ruleList.stream()
            .map(rule -> mapIntegrationResult(rule, recordId))
            .collect(Collectors.toList());
    integrationResultService.saveBatch(collect);
  }

  @Override
  public ResultVo<Boolean> existRunningIntegration(Long taskId) {
    // ?????????????????????????????????????????????
    IntegrationRecordsPO lastRecord = integrationRecordsService.getLastIntegrationRecord(taskId);
    if (lastRecord == null) {
      // ??????????????????
      return ResultVo.createSuccess(false);
    }
    // ???????????????????????????????????????????????????????????????
    return lastRecord.getIntegrateStatus() == 0 ? ResultVo.createSuccess(true) : ResultVo.createSuccess(false);
  }

  @Override
  public PageVO<DatasetInfoVO> getIntegrationLabelPage(IntegrationReq req) {
    IntegrateLabelResultPO po = new IntegrateLabelResultPO();
    po.setTaskId(req.getTaskId());
    po.setSentence(req.getSentence());
    po.setLabelId(req.getLabelId());
    po.setDataType(req.getDataType());
    Page<Object> page = Page.of(req.getCurPage(), req.getPageSize());
    PageVO<IntegrateLabelResultPO> pageVO = integrateLabelResultService.selectPageByTaskIdAndKeyword(
        page, po);
    return pageVO.convert(this::mapDatasetInfoVO);
  }

  @Override
  public ResultVo<IntegrateOverviewVO> getIntegrateOverview(TaskIdReq req) {
    IntegrationRecordsPO record = integrationRecordsService.getLastSuccessLabeledRecord(
        req.getTaskId());
    if (record == null) {
      return ResultVo.createSuccess(null);
    }
    IntegrateOverviewVO vo = new IntegrateOverviewVO();
    BeanCopyUtil.copy(record, vo);
    vo.setTimeCost(String.valueOf(record.getTimeCost()));
    vo.setLastUpdateTime(record.getUpdateDatetime());
    return ResultVo.createSuccess(vo);
  }

  @Override
  public ResultVo<Long> autoLabel(TaskIdReq req) {
    // 1. ?????????????????????????????????
    IntegrationRecordsPO record = integrationRecordsService.getLastIntegrationRecord(req.getTaskId());
    // ?????????????????????????????????????????????????????????
    if (record == null) {
      return ResultVo.create(NOT_INTEGRATION, false, null);
    }
    // ???????????????????????????????????????????????????????????????????????????????????????
    if (record.getIntegrateStatus() == 0
        && record.getUpdateDatetime().isAfter(LocalDateTime.now().minusHours(1))) {
      return ResultVo.create(WAIT_INTEGRATION, false, null);
    }
    // ????????????????????????????????????????????????
    if (record.getIntegrateStatus() == 2) {
      return ResultVo.create(INTEGRATION_FAILED, false, null);
    }
    // ???????????????????????????????????????????????????
    if (record.getLabeled() == 2) {
      // ????????????????????????????????????????????????????????????????????????????????????
      return ResultVo.createSuccess(record.getId());
    } else if (record.getLabeled() == 3) {
      // ??????????????????????????????????????????????????????
      return ResultVo.createSuccess(commitAutoLabel(record.getTaskId(), record.getId()));
    } else if (record.getLabeled() == 1) {
      // ?????????????????????????????????????????????????????????
      return ResultVo.createSuccess(record.getId());
    } else {
      // ???????????????????????????????????????
      return ResultVo.createSuccess(commitAutoLabel(record.getTaskId(), record.getId()));
    }
  }

  @Override
  public ResultVo<Boolean> existLabelingTask(Long taskId) {
    // ???????????????????????????????????????????????????????????????????????????????????????????????????
    IntegrationRecordsPO lastRecord = integrationRecordsService.getLastIntegrationRecord(taskId);
    if (lastRecord == null) {
      // ??????????????????
      return ResultVo.createSuccess(false);
    }
    // ???????????????????????????????????????????????????????????????
    return lastRecord.getLabeled() == 1 ? ResultVo.createSuccess(true) : ResultVo.createSuccess(false);
  }

  @Override
  public ResultVo<Boolean> saveIntegrationAsync(IntegrateCallbackDTO integrateCallbackDTO) {
    // ?????????????????????redis
    log.info("[IntegrationServiceImpl.saveIntegrationAsync] ???????????????????????????{}", integrateCallbackDTO);
    String key = RedisKeyMethods.getIntegrateCacheKey(integrateCallbackDTO.getTaskId(),
        integrateCallbackDTO.getRecordId());
    String value;
    try {
      value = objectMapper.writeValueAsString(integrateCallbackDTO);
    } catch (JsonProcessingException e) {
      log.error("[IntegrationServiceImpl.saveIntegrationAsync] ????????????????????????json??????????????????{}", e.getMessage());
      stringRedisTemplate.opsForValue().set(key, " ", 10, TimeUnit.MINUTES);
      return ResultVo.create("????????????", -1, false, null);
    }
    // ?????????????????????10??????
    stringRedisTemplate.opsForValue().set(key, value, 10, TimeUnit.MINUTES);
    return ResultVo.createSuccess(true);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> delAutoLabelResult(AutoLabelResultReq req) {
    List<Long> recordIds = req.getRecordIds();
    boolean result = integrateLabelResultService.removeBatchByIds(recordIds);
    //updateOverviewAfterUpdate(req.getTaskId());
    return ResultVo.createSuccess(result);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> editAutoLabelResult(AutoLabelResultReq req) {
    LabelInfoPO infoPO = labelInfoService.selectOneByTaskAndLabel(req.getTaskId(), req.getLabelId());
    if (infoPO == null) {
      return ResultVo.create("???????????????", -1, false, false);
    }
    IntegrateLabelResultPO labelResultPO = integrateLabelResultService.getById(req.getRecordId());
    if (labelResultPO == null) {
      return ResultVo.create("???????????????", -1, false, false);
    }
    // ????????????????????????????????????
    DatasetInfoPO datasetInfo = datasetInfoService.getLastDatasetInfo(req.getTaskId());
    TestDataPO testDataPO = new TestDataPO();
    testDataPO.setTaskId(req.getTaskId());
    testDataPO.setDataSetId(datasetInfo.getId());
    testDataPO.setLabel(req.getLabelId());
    testDataPO.setSentence(labelResultPO.getSentence());
    testDataPO.setLabelDes(infoPO.getLabelDesc());
    testDataPO.setShowData(1);
    testDataPO.setDataType(DatasetType.TRAIN.getType());
    testDataService.insertAndAutoIncrement(testDataPO);
    // ???????????????????????????
    integrateLabelResultService.removeById(labelResultPO.getId());
    // ????????????????????????
    unlabelDataService.deleteByTaskAndDataId(req.getTaskId(), labelResultPO.getSentenceId());
    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????
    ruleUnlabeledResultService.delByTaskIdAndSentenceId(req.getTaskId(), labelResultPO.getSentenceId());
    //updateOverviewAfterUpdate(req.getTaskId());
    // ?????????????????????
    DatasetDetailPO unlabelData = datasetDetailService.selectByTaskIdAndType(req.getTaskId(),
        DatasetType.UNLABELED.getType());
    unlabelData.setUpdateDatetime(LocalDateTime.now());
    datasetDetailService.updateById(unlabelData);
    // ?????????????????????????????????????????????????????????????????????????????????
    DatasetDetailPO trainData = datasetDetailService.selectByTaskIdAndType(req.getTaskId(),
        DatasetType.TRAIN.getType());
    if (trainData == null) {
      trainData = new DatasetDetailPO();
      trainData.setDataSetId(unlabelData.getDataSetId());
      trainData.setFileType(DatasetType.TRAIN.getType());
      trainData.setTaskId(unlabelData.getTaskId());
      String prefix = unlabelData.getFileAddress().substring(0, unlabelData.getFileAddress().lastIndexOf("/") + 1);
      trainData.setFileAddress(prefix + FileConstant.TRAIN_DATA_NAME + ".json");
      trainData.setUpdateDatetime(LocalDateTime.now());
      datasetDetailService.save(trainData);
    } else {
      trainData.setUpdateDatetime(LocalDateTime.now());
      datasetDetailService.updateById(trainData);
    }
    return ResultVo.createSuccess(true);
  }

  @Override
  public ResultVo<Long> autoLabelNer(TaskIdReq req) {
    DatasetInfoPO datasetInfo = datasetInfoService.getLastDatasetInfo(req.getTaskId());
    if (datasetInfo == null) {
      return ResultVo.create("???????????????????????????", -1, false, null);
    }
    // ?????????????????????????????????????????????????????????????????????
    int unLabeledData =
        nerTestDataService.countUnLabeledData(req.getTaskId(), DatasetType.TRAIN.getType());
    if (unLabeledData > 0) {
      return ResultVo.create("???????????????????????????????????????????????????????????????????????????????????????", -1, false, null);
    }
    unLabeledData =
        nerTestDataService.countUnLabeledData(req.getTaskId(), DatasetType.TEST.getType());
    if (unLabeledData > 0) {
      return ResultVo.create("???????????????????????????????????????????????????????????????????????????????????????", -1, false, null);
    }
    // 1. ?????????????????????????????????
    IntegrationRecordsPO record = integrationRecordsService.getLastIntegrationRecord(req.getTaskId());
    // ??????????????????????????????NER??????????????????
    if (record == null) {
      return ResultVo.createSuccess(commitNerAutoLabel(req.getTaskId(), datasetInfo.getId()));
    }
    if (record.getLabeled() == 1) {
      return ResultVo.create("????????????????????????????????????????????????????????????", -1, false, null);
    }
    return ResultVo.createSuccess(commitNerAutoLabel(req.getTaskId(), datasetInfo.getId()));
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> saveFewShotAsync(FewShotCallbackDTO callbackDTO) {
    Long taskId = callbackDTO.getTaskId();
    String key = RedisKeyMethods.getFewShowKey(taskId, callbackDTO.getRecordId());
    String uid = stringRedisTemplate.opsForValue().get(key);
    if (StringUtils.isBlank(uid)) {
      return ResultVo.create("????????????", -1, false, false);
    }
    ResultDTO results = callbackDTO.getResults();
    // ????????????
    if (!callbackDTO.getState() || results == null) {
      // ??????????????????
      fewShotFail(callbackDTO, uid);
      ResultVo<Boolean> resultVo = ResultVo.createSuccess(false);
      if (results == null) {
        log.error("[IntegrationServiceImpl.saveFewShotAsync] ????????????????????????????????????");
        resultVo.setMsg("?????????????????????????????????????????????");
      } else {
        resultVo.setMsg(callbackDTO.getDetail());
      }
      return resultVo;
    }
    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    RuleInfoPO rule = ruleInfoService.getRuleByType(taskId, RuleType.FEW.getType());
    if (rule == null) {
      rule = new RuleInfoPO();
      rule.setCompleted(1);
      rule.setTaskId(taskId);
      rule.setRuleName(RuleType.FEW.getName());
      rule.setRuleType(RuleType.FEW.getType());
      ruleInfoService.save(rule);
    } else {
      rule.setUpdateDatetime(LocalDateTime.now());
      ruleInfoService.updateById(rule);
      ruleUnlabeledResultService.deleteByTaskIdAndRuleId(taskId, rule.getId());
      ruleResultService.deleteByTaskIdAndRuleId(taskId, rule.getId());
    }
    // ??????????????????????????????????????????????????????????????????????????????ID????????????
    DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId,
        DatasetType.UNLABELED.getType());
    String localTemp = fileUploadProperties.getTempPath();
    String fileName = NanoId.randomNanoId() + "_" + taskId + ".json";
    Path path = FileMethods.prepareFile(localTemp, fileName);
    try {
      List<Integer> labelResults = results.getUnlabeledPredictions();
      if (labelResults != null && labelResults.size() > 0) {
        ossService.download(detailPO.getFileAddress(), path.toAbsolutePath().toString());
        // ????????????,??????????????????
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
          int length = 0;
          int s = labelResults.size();
          int size = Math.min(s, 10000);
          List<RuleUnlabeledResultPO> unlabelDataPOList = new ArrayList<>(size);
          String line;
          while ((line = reader.readLine()) != null) {
            TextLabelDataModel model = objectMapper.readValue(line, TextLabelDataModel.class);
            RuleUnlabeledResultPO po = new RuleUnlabeledResultPO();
            po.setTaskId(taskId);
            po.setRuleId(rule.getId());
            po.setSentenceId(model.getId());
            po.setLabelId(length >= s ? -1 : labelResults.get(length));
            unlabelDataPOList.add(po);
            length++;
            if (unlabelDataPOList.size() == size) {
              ruleUnlabeledResultService.saveForBatchNoLog(unlabelDataPOList);
              unlabelDataPOList.clear();
            }
          }
          if (unlabelDataPOList.size() > 0) {
            ruleUnlabeledResultService.saveForBatchNoLog(unlabelDataPOList);
            unlabelDataPOList.clear();
          }
        } catch (IOException e) {
          throw new BusinessIllegalStateException(e);
        }
      }
      // ????????????????????????????????????????????????
      List<TestDataPO> allShowByTaskId = testDataService.getAllShowByTaskId(taskId);
      List<RuleResultPO> ruleResultPOList = new ArrayList<>(allShowByTaskId.size());
      List<Integer> valPredictions = results.getValPredictions();
      if (valPredictions == null) {
        valPredictions = new ArrayList<>();
      }
      for (int i = 0, size = allShowByTaskId.size(); i < size; i++) {
        TestDataPO testDataPO = allShowByTaskId.get(i);
        RuleResultPO ruleResultPO = new RuleResultPO();
        ruleResultPO.setTaskId(taskId);
        ruleResultPO.setRuleId(rule.getId());
        ruleResultPO.setSentenceId(testDataPO.getDataId());
        ruleResultPO.setLabelId(i >= valPredictions.size() ? -1 : valPredictions.get(i));
        ruleResultPO.setShowData(1);
        ruleResultPO.setDataType(testDataPO.getDataType());
        ruleResultPOList.add(ruleResultPO);
      }
      ruleResultService.saveForBatchNoLog(ruleResultPOList);
      ruleResultPOList.clear();
      List<TestDataPO> unShowByTaskId = testDataService.getAllUnShowByTaskId(taskId);
      List<Integer> testPredictions = results.getTestPredictions();
      if (testPredictions == null) {
        testPredictions = new ArrayList<>();
      }
      ruleResultPOList = new ArrayList<>(unShowByTaskId.size());
      for (int i = 0, size = unShowByTaskId.size(); i < size; i++) {
        TestDataPO testDataPO = unShowByTaskId.get(i);
        RuleResultPO ruleResultPO = new RuleResultPO();
        ruleResultPO.setTaskId(taskId);
        ruleResultPO.setRuleId(rule.getId());
        ruleResultPO.setShowData(0);
        ruleResultPO.setDataType(testDataPO.getDataType());
        ruleResultPO.setSentenceId(testDataPO.getDataId());
        ruleResultPO.setLabelId(i >= testPredictions.size() ? -1 : testPredictions.get(i));
        ruleResultPOList.add(ruleResultPO);
      }
      ruleResultService.saveForBatchNoLog(ruleResultPOList);
      // ????????????????????????
      // ???????????????????????????????????????????????????????????????????????????
      String textClickCacheKey = getTextClickCacheKey(taskId, callbackDTO.getRecordId());
      Boolean hasKey = stringRedisTemplate.hasKey(textClickCacheKey);
      if (Boolean.FALSE.equals(hasKey)) {
        commitIntegration(taskId, callbackDTO.getRecordId(), uid);
      }
    } finally {
      try {
        Files.delete(path);
      } catch (IOException e) {
        log.error("[IntegrationServiceImpl.saveFewShotAsync]", e);
      }
    }
    return ResultVo.createSuccess(true);
  }

  private void fewShotFail(FewShotCallbackDTO callbackDTO, String uid) {
    Long recordId = callbackDTO.getRecordId();
    IntegrationRecordsPO integrationRecordsPO = new IntegrationRecordsPO();
    integrationRecordsPO.setId(recordId);
    integrationRecordsPO.setIntegrateStatus(2);
    integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
    integrationRecordsService.updateById(integrationRecordsPO);
  }

  private void updateOverviewAfterUpdate(Long taskId) {
    IntegrationRecordsPO record = integrationRecordsService.getLastSuccessLabeledRecord(taskId);
    long sentenceCount = integrateLabelResultService.countByTaskId(taskId);
    int labelCount = integrateLabelResultService.countLabelByTaskId(taskId);
    IntegrationRecordsPO updateRecord = new IntegrationRecordsPO();
    updateRecord.setId(record.getId());
    updateRecord.setUpdateDatetime(LocalDateTime.now());
    updateRecord.setTrainSentenceCount(sentenceCount);
    updateRecord.setTrainLabelCount(labelCount);
    long totalSentence = unlabelDataService.countUnlabelDataByTaskId(taskId);
    if (totalSentence > 0) {
      updateRecord.setUnlabelCoverage(CalcUtil
          .multiply(CalcUtil.divide(sentenceCount, totalSentence, 2), "100", 2));
    } else {
      updateRecord.setUnlabelCoverage("0");
    }
    integrationRecordsService.updateById(updateRecord);
  }

  private DatasetInfoVO mapDatasetInfoVO(IntegrateLabelResultPO po) {
    DatasetInfoVO vo = new DatasetInfoVO();
    vo.setId(po.getId());
    vo.setDataId(po.getSentenceId());
    vo.setSentence(po.getSentence());
    vo.setLabel(po.getLabelId().toString());
    vo.setLabelDes(po.getLabelDes());
    vo.setDataType(po.getDataType());
    return vo;
  }

  private IntegrationResultPO mapIntegrationResult(RuleInfoPO rule, Long integrationId) {
    IntegrationResultPO resultPO = new IntegrationResultPO();
    resultPO.setTaskId(rule.getTaskId());
    resultPO.setIntegrationId(integrationId);
    resultPO.setDatasetId(rule.getDatasetId());
    resultPO.setRuleId(rule.getId());
    resultPO.setLabelId(rule.getLabel());
    resultPO.setLabelDes(rule.getLabelDes());
    resultPO.setAccuracy(rule.getAccuracy());
    resultPO.setCoverage(rule.getCoverage());
    resultPO.setRepeat(rule.getOverlap());
    resultPO.setConflict(rule.getConflict());
    return resultPO;
  }

  /**
   * ???????????????????????????
   *
   * @param taskId         ??????id
   * @param createDatetime ???????????????????????????????????????
   * @return ????????????
   */
  private ResultVo<Long> commitFewShot(Long taskId, Integer datasetId, LocalDateTime createDatetime) {
    IntegrationRecordsPO po = new IntegrationRecordsPO();
    po.setIntegrateStatus(0);
    po.setTaskId(taskId);
    po.setDatasetId(datasetId);
    integrationRecordsService.save(po);
    // ????????????????????????????????????????????????????????????????????????????????????????????????
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setRecordId(po.getId());
    algorithmTask.setType(AlgorithmTaskType.FEW_SHOT);
    HashMap<String, Object> map = new HashMap<>();
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    map.put(SESSION_UID, userInfo.getUid());
    map.put(UPDATE_TIME, createDatetime);
    algorithmTask.setParams(map);
    algoTaskAppendEventPublisher.publish(algorithmTask);
    return ResultVo.createSuccess(po.getId());
  }

  private ResultVo<Long> commitIntegrationDirect(Long taskId, Integer datasetId, LocalDateTime createDatetime) {
    // ??????????????????
    IntegrationRecordsPO po = new IntegrationRecordsPO();
    po.setIntegrateStatus(0);
    po.setTaskId(taskId);
    po.setDatasetId(datasetId);
    integrationRecordsService.save(po);
    commitIntegration(taskId, po.getId(), ServletUserHolder.getUserByContext().getUid());
    return ResultVo.createSuccess(po.getId());
  }

  private void commitIntegration(Long taskId, Long recordId, String uid) {
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setRecordId(recordId);
    algorithmTask.setType(AlgorithmTaskType.INTEGRATION);
    HashMap<String, Object> map = new HashMap<>();
    map.put(SESSION_UID, uid);
    algorithmTask.setParams(map);
    algoTaskAppendEventPublisher.publish(algorithmTask);
  }

  private Long commitAutoLabel(Long taskId, Long recordId) {
    IntegrationRecordsPO po = new IntegrationRecordsPO();
    po.setLabeled(1);
    po.setId(recordId);
    integrationRecordsService.updateById(po);
    // ??????????????????
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setRecordId(recordId);
    algorithmTask.setType(AlgorithmTaskType.AUTO_LABEL);
    HashMap<String, Object> map = new HashMap<>();
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    map.put(SESSION_UID, userInfo.getUid());
    algorithmTask.setParams(map);
    algoTaskAppendEventPublisher.publish(algorithmTask);
    return recordId;
  }

  private Long commitNerAutoLabel(Long taskId, Integer datasetId) {
    IntegrationRecordsPO po = new IntegrationRecordsPO();
    po.setIntegrateStatus(1);
    po.setTaskId(taskId);
    po.setDatasetId(datasetId);
    po.setLabeled(1);
    long labelSize = labelInfoService.countLabelInfoByTaskId(taskId);
    po.setTrainLabelCount((int) labelSize);
    integrationRecordsService.save(po);
    // ??????????????????
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setRecordId(po.getId());
    algorithmTask.setType(AlgorithmTaskType.NER_AUTO_LABEL);
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    HashMap<String, Object> map = new HashMap<>();
    map.put(SESSION_UID, userInfo.getUid());
    algorithmTask.setParams(map);
    algoTaskAppendEventPublisher.publish(algorithmTask);
    return po.getId();
  }

  @Autowired
  public void setIntegrationResultService(IntegrationResultService integrationResultService) {
    this.integrationResultService = integrationResultService;
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
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setAlgoTaskAppendEventPublisher(
      AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher) {
    this.algoTaskAppendEventPublisher = algoTaskAppendEventPublisher;
  }

  @Autowired
  public void setIntegrateLabelResultService(
      IntegrateLabelResultService integrateLabelResultService) {
    this.integrateLabelResultService = integrateLabelResultService;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setNerTestDataService(NerTestDataService nerTestDataService) {
    this.nerTestDataService = nerTestDataService;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
  }

  @Autowired
  public void setOssService(OssServiceImpl ossService) {
    this.ossService = ossService;
  }

  @Autowired
  public void setRuleUnlabeledResultService(RuleUnlabeledResultService ruleUnlabeledResultService) {
    this.ruleUnlabeledResultService = ruleUnlabeledResultService;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void setRuleResultService(RuleResultService ruleResultService) {
    this.ruleResultService = ruleResultService;
  }
}
