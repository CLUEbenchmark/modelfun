package com.wl.xc.modelfun.tasks.algorithm.handlers;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_USER_ID;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.UPDATE_TIME;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskTrainKey;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickCacheKey;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickErrorKey;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickTaskKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.ModelType;
import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.entities.dto.LabelDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.dto.lfs.BuiltinRuleDTO;
import com.wl.xc.modelfun.entities.dto.lfs.LabelRuleDTO;
import com.wl.xc.modelfun.entities.dto.lfs.RegexRuleDTO;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule;
import com.wl.xc.modelfun.entities.model.RegexRule;
import com.wl.xc.modelfun.entities.model.RegexRule4Data;
import com.wl.xc.modelfun.entities.model.TextOneClickInput;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.TrainRecordsPO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.IntegrateLabelResultService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.IntegrationService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.TestDataService;
import com.wl.xc.modelfun.service.TrainRecordsService;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmHandler;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.rule.GlobalRuleParameterCalc;
import com.wl.xc.modelfun.tasks.rule.RuleHandleService;
import com.wl.xc.modelfun.tasks.rule.RuleTask;
import com.wl.xc.modelfun.utils.BeanCopyUtil;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * ?????????????????????????????????
 *
 * @version 1.0
 * @date 2022/5/31 15:13
 */
@Slf4j
@Component
public class TextOneClickHandler implements AlgorithmHandler {

  private RuleHandleService ruleHandleService;

  private IntegrationSubHandler integrationHandler;

  private GlobalRuleParameterCalc globalRuleParameterCalc;

  private DatasetInfoService datasetInfoService;

  private IntegrationRecordsService integrationRecordsService;

  private AutoLabelSubHandler autoLabelSubHandler;

  private StringRedisTemplate stringRedisTemplate;

  private TrainRecordsService trainRecordsService;

  private IntegrateLabelResultService integrateLabelResultService;

  private LabelInfoService labelInfoService;

  private RuleInfoService ruleInfoService;

  private ModelTrainSubHandler modelTrainSubHandler;

  private TaskInfoService taskInfoService;

  private RestTemplate restTemplate;

  private AlgorithmProperties algorithmProperties;

  private OssService ossService;

  private DatasetDetailService datasetDetailService;

  private IntegrationService integrationService;

  private ObjectMapper objectMapper;

  private FewShotSubHandler fewShotSubHandler;

  private TestDataService testDataService;

  private RuleResultService ruleResultService;

  private RuleUnlabeledResultService ruleUnlabeledResultService;

  private final ThreadLocal<Integer> STATE_HOLDER = new ThreadLocal<>();

  @Override
  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.TEXT_ONE_CLICK;
  }

  @Override
  public void handle(AlgorithmTask task) {
    Long taskId = task.getTaskId();
    TaskInfoPO infoPO = taskInfoService.getById(taskId);
    String uid = (String) task.getParams().get(SESSION_UID);
    Integer userId = (Integer) task.getParams().get(SESSION_USER_ID);
    try {
      // ????????????????????????lf?????????????????????
      List<RuleInfoPO> rules = getRules(taskId, infoPO);
      // ????????????????????????
      noticeState(userId, 2, infoPO);
      labelRules(rules, taskId);
      // ??????????????????
      noticeState(userId, 3, infoPO);
      IntegrationRecordsPO po = integrateRules(taskId, uid);
      // ??????????????????
      noticeState(userId, 4, infoPO);
      autoLabel(taskId, po);
      // ??????????????????
      noticeState(userId, 5, infoPO);
      po = integrationRecordsService.getById(po.getId());
      trainModel(uid, taskId, po);
      noticeState(userId, 6, infoPO);
    } catch (Exception e) {
      handleError(task, e, infoPO);
    } finally {
      clearResource(taskId);
    }
  }

  private List<RuleInfoPO> getRules(Long taskId, TaskInfoPO infoPO) {
    STATE_HOLDER.set(1);
    // ????????????
    if (needReGenerate(taskId)) {
      // ???????????????????????????????????????????????????????????????
      RuleInfoPO po = new RuleInfoPO();
      po.setTaskId(taskId);
      po.setAutoGenerated(true);
      List<RuleInfoPO> ruleInfoPOS = ruleInfoService.selectBySelective(po);
      List<Long> list = ruleInfoPOS.stream().map(RuleInfoPO::getId).collect(Collectors.toList());
      deleteRuleAndLabel(list, taskId);
      log.info("[TextOneClickHandler.getRules] ?????????????????????????????????????????????????????????{}", ruleInfoPOS.size());
    } else {
      // ??????????????????????????????????????????????????????????????????????????????
      // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
      // ??????????????????????????????????????????????????????????????????????????????
      return Collections.emptyList();
    }
    TextOneClickInput datasetInput = new TextOneClickInput();
    datasetInput.setName(infoPO.getName());
    datasetInput.setTaskType(infoPO.getTaskType().toString());
    datasetInput.setDomainType(infoPO.getDomain());
    datasetInput.setKeywords(infoPO.getKeyword());
    datasetInput.setDescription(infoPO.getDescription());
    // ???????????????
    long timeOut = 1800 * 1000;
    DatasetDetailPO detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.UNLABELED.getType());
    datasetInput.setTrainPath(ossService.getUrlSigned(detailPO.getFileAddress(), timeOut));
    detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_SHOW.getType());
    datasetInput.setValPath(ossService.getUrlSigned(detailPO.getFileAddress(), timeOut));
    detailPO = datasetDetailService.selectByTaskIdAndType(taskId, DatasetType.TEST_UN_SHOW.getType());
    datasetInput.setTestPath(ossService.getUrlSigned(detailPO.getFileAddress(), timeOut));
    List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(taskId);
    List<LabelDTO> collect = labelInfoPOS.stream().map(v -> {
      LabelDTO dto = new LabelDTO();
      dto.setLabelId(v.getLabelId());
      dto.setLabelDesc(v.getLabelDesc());
      return dto;
    }).collect(Collectors.toList());
    datasetInput.setNumClass((long) collect.size());
    datasetInput.setLabels(collect);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    RequestEntity<TextOneClickInput> requestEntity;
    try {
      requestEntity = new RequestEntity<>(datasetInput, headers, HttpMethod.POST,
          new URI(algorithmProperties.getGenLfUrl()));
    } catch (URISyntaxException e) {
      throw new BusinessIllegalStateException("?????????????????????", e);
    }
    ResponseEntity<List<LabelRuleDTO<?>>> result;
    try {
      result = restTemplate.exchange(requestEntity,
          new ParameterizedTypeReference<>() {
          });
    } catch (RestClientException e) {
      throw new BusinessIllegalStateException("????????????????????????", e);
    }
    DatasetInfoPO datasetInfo = datasetInfoService.getLastDatasetInfo(taskId);
    List<RuleInfoPO> list = new ArrayList<>();
    List<LabelRuleDTO<?>> body = result.getBody();
    if (body == null || body.isEmpty()) {
      throw new BusinessIllegalStateException("????????????????????????");
    }
    for (LabelRuleDTO<?> ruleDTO : body) {
      RuleInfoPO ruleInfoPO = new RuleInfoPO();
      BeanCopyUtil.copy(ruleDTO, ruleInfoPO);
      try {
        if (ruleDTO instanceof BuiltinRuleDTO) {
          BuiltinModelRule metadata = ((BuiltinRuleDTO) ruleDTO).getMetadata();
          ruleInfoPO.setMetadata(objectMapper.writeValueAsString(metadata));
        } else if (ruleDTO instanceof RegexRuleDTO) {
          RegexRule metadata = ((RegexRuleDTO) ruleDTO).getMetadata();
          ruleInfoPO.setMetadata(
              objectMapper.writeValueAsString(
                  Collections.singletonList(
                      Collections.singletonList(new RegexRule4Data(metadata)))));
        }
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("????????????????????????", e);
      }
      ruleInfoPO.setTaskId(taskId);
      ruleInfoPO.setDatasetId(datasetInfo.getId());
      ruleInfoPO.setCreateDatetime(LocalDateTime.now());
      ruleInfoPO.setUpdateDatetime(LocalDateTime.now());
      ruleInfoPO.setCreateStartTime(LocalDateTime.now());
      ruleInfoPO.setCreateEndTime(LocalDateTime.now());
      ruleInfoPO.setAutoGenerated(true);
      ruleInfoService.save(ruleInfoPO);
      list.add(ruleInfoPO);
    }
    return list;
  }

  private boolean needReGenerate(Long taskId) {
    List<RuleInfoPO> list = ruleInfoService.getRuleListByTaskId(taskId);
    // ??????????????????3????????????????????????
    if (list.size() < 3) {
      return true;
    }
    long count = list.stream().filter(RuleInfoPO::getAutoGenerated).count();
    // ???????????????????????????????????????????????????????????????????????????
    return count == 0;
  }

  private void labelRules(List<RuleInfoPO> rules, Long taskId) {
    STATE_HOLDER.set(2);
    setState(taskId, "2");
    // ?????????????????????????????????????????????????????????
    RuleInfoPO po = new RuleInfoPO();
    po.setTaskId(taskId);
    po.setCompleted(2);
    List<RuleInfoPO> failedRules = ruleInfoService.selectBySelective(po);
    List<RuleInfoPO> combine = new ArrayList<>();
    combine.addAll(rules);
    combine.addAll(failedRules);
    // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    try {
      for (RuleInfoPO rule : combine) {
        RuleTask task = new RuleTask();
        task.setType(RuleTaskType.SINGLE);
        task.setTaskId(taskId);
        task.setRuleInfo(rule);
        ruleHandleService.calculate(task);
      }
      // ??????????????????
      RuleTask task = new RuleTask();
      task.setTaskId(taskId);
      globalRuleParameterCalc.globalCalc(task);
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException("??????????????????, " + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
    } finally {
      removeLowRateRule(rules, taskId);
    }
  }

  private void removeLowRateRule(List<RuleInfoPO> rules, Long taskId) {
    List<RuleInfoPO> list = ruleInfoService.getRuleListByTaskIdAndType(taskId,
        RuleType.GPT3.getType());
    // ??????????????????????????????
    list.removeIf(rule -> !rule.getAutoGenerated());
    Set<Long> currentBuiltinRule = rules.stream()
        .filter(r -> RuleType.GPT3.getType().equals(r.getRuleType()))
        .map(RuleInfoPO::getId).collect(Collectors.toSet());
    List<RuleInfoPO> removeList = new ArrayList<>();
    for (RuleInfoPO ruleInfoPO : list) {
      if (currentBuiltinRule.contains(ruleInfoPO.getId())) {
        if (StringUtils.isNotBlank(ruleInfoPO.getAccuracy())
            && Double.parseDouble(ruleInfoPO.getAccuracy()) < 30) {
          log.warn("[TextOneClickHandler.removeLowRateRule] ?????? {} ???????????? {}?????????0.3???????????????",
              ruleInfoPO.getId(), ruleInfoPO.getAccuracy());
          removeList.add(ruleInfoPO);
        }
      }
    }
    List<Long> ruleIds = removeList.stream().map(RuleInfoPO::getId).collect(Collectors.toList());
    rules.removeAll(removeList);
    deleteRuleAndLabel(ruleIds, taskId);
  }

  private IntegrationRecordsPO integrateRules(Long taskId, String uid) {
    STATE_HOLDER.set(3);
    setState(taskId, "3");
    // ????????????
    try {
      // ????????????????????????????????????????????????????????????????????????
      DatasetInfoPO datasetInfo = datasetInfoService.getLastDatasetInfo(taskId);
      // ???????????????????????????
      ResultVo<Long> vo = integrationService.checkBeforeIntegrate(taskId, datasetInfo);
      if (!vo.getSuccess()) {
        throw new BusinessIllegalStateException(vo.getMsg(), vo.getCode());
      }
      IntegrationRecordsPO po = fewShotLearn(taskId, datasetInfo.getId(), uid);
      AlgorithmTask task = new AlgorithmTask();
      task.setTaskId(taskId);
      task.setRecordId(po.getId());
      // ????????????
      integrationHandler.handle(task);
      return po;
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException("??????????????????, " + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
    }
  }

  private IntegrationRecordsPO fewShotLearn(Long taskId, Integer id, String uid) {
    IntegrationRecordsPO lastRecord = integrationRecordsService.getLastIntegrationRecord(taskId);
    IntegrationRecordsPO po = new IntegrationRecordsPO();
    po.setTaskId(taskId);
    po.setDatasetId(id);
    po.setIntegrateStatus(0);
    // ???????????????????????????????????????????????????????????????
    integrationRecordsService.save(po);
    long count = testDataService.countByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
    // ?????????????????????????????????????????????????????????
    if (count == 0) {
      return po;
    }
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setRecordId(po.getId());
    HashMap<String, Object> map = new HashMap<>();
    if (lastRecord != null) {
      map.put(UPDATE_TIME, lastRecord.getCreateDatetime());
    }
    map.put(SESSION_UID, uid);
    algorithmTask.setParams(map);
    algorithmTask.setType(AlgorithmTaskType.FEW_SHOT);
    log.info("[TextOneClickHandler.fewShotLearn] ???????????????????????????");
    String textClickCacheKey = getTextClickCacheKey(taskId, po.getId());
    stringRedisTemplate.opsForValue().set(textClickCacheKey, "1", 60, TimeUnit.MINUTES);
    fewShotSubHandler.handle(algorithmTask);
    String key = RedisKeyMethods.getFewShowKey(taskId, po.getId());
    while (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
      }
    }
    String errorKey = getTextClickErrorKey(taskId, po.getId());
    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(errorKey))) {
      String errorMsg = stringRedisTemplate.opsForValue().get(errorKey);
      stringRedisTemplate.delete(errorKey);
      throw new BusinessIllegalStateException("????????????????????????" + errorMsg);
    }
    return po;
  }

  private void autoLabel(Long taskId, IntegrationRecordsPO po) {
    STATE_HOLDER.set(4);
    setState(taskId, "4");
    try {
      IntegrationRecordsPO copy = new IntegrationRecordsPO();
      copy.setLabeled(1);
      copy.setId(po.getId());
      copy.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsService.updateById(copy);
      // ??????????????????
      AlgorithmTask algorithmTask = new AlgorithmTask();
      algorithmTask.setTaskId(taskId);
      algorithmTask.setRecordId(po.getId());
      algorithmTask.setType(AlgorithmTaskType.AUTO_LABEL);
      // ??????????????????
      autoLabelSubHandler.handle(algorithmTask);
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException("??????????????????, " + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
    }
  }

  private void trainModel(String uid, Long taskId, IntegrationRecordsPO integrationRecordsPO) {
    STATE_HOLDER.set(5);
    setState(taskId, "5");
    TrainRecordsPO po;
    try {
      po = new TrainRecordsPO();
      po.setTrainStatus(0);
      po.setTaskId(taskId);
      po.setDatasetId(integrationRecordsPO.getDatasetId());
      po.setModelType(ModelType.BERT.getType());
      // ???????????????
      // ???????????????
      long count = integrateLabelResultService.countCorrectByTaskId(taskId);
      long trainCount = testDataService.countByTaskIdAndType(taskId, DatasetType.TRAIN.getType());
      po.setTrainCount((int) count + (int) trainCount);
      // ????????????
      long labelCount = labelInfoService.countLabelInfoByTaskId(taskId);
      po.setLabelCount((int) labelCount);
      // ????????????
      Long ruleCount = ruleInfoService.countRuleComplete(taskId);
      po.setRuleCount(ruleCount.intValue());
      trainRecordsService.save(po);
      String textClickCacheKey = getTextClickCacheKey(taskId, po.getId());
      stringRedisTemplate.opsForValue().set(textClickCacheKey, "1", 13 * 60, TimeUnit.MINUTES);
      AlgorithmTask algorithmTask = new AlgorithmTask();
      algorithmTask.setRecordId(po.getId());
      algorithmTask.setTaskId(taskId);
      algorithmTask.setType(AlgorithmTaskType.MODEL_TRAIN);
      Map<String, Object> params = new HashMap<>();
      params.put("model", ModelType.BERT.getType());
      params.put("testLabel", integrationRecordsPO.getResultFileAddress());
      params.put(SESSION_UID, uid);
      algorithmTask.setParams(params);
      // ????????????????????????
      modelTrainSubHandler.handle(algorithmTask);
    } catch (BusinessException e) {
      throw new BusinessIllegalStateException("??????????????????, " + e.getMessage(), e);
    } catch (Exception e) {
      throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
    }
    // ??????????????????
    long timeOut = System.currentTimeMillis() + TimeUnit.MINUTES.toHours(2);
    String taskTrainKey = getTaskTrainKey(taskId, po.getId());
    while (System.currentTimeMillis() < timeOut
        && Boolean.TRUE.equals(stringRedisTemplate.hasKey(taskTrainKey))) {
      try {
        TimeUnit.SECONDS.sleep(5);
      } catch (InterruptedException e) {
        throw new BusinessIllegalStateException("??????????????????????????????????????????", e);
      }
    }
    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(taskTrainKey))) {
      throw new BusinessIllegalStateException("?????????????????????");
    }
    String errorKey = getTextClickErrorKey(taskId, po.getId());
    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(errorKey))) {
      String errorMsg = stringRedisTemplate.opsForValue().get(errorKey);
      stringRedisTemplate.delete(errorKey);
      throw new BusinessIllegalStateException("?????????????????????" + errorMsg);
    }
  }

  private void noticeState(Integer userId, int state, TaskInfoPO taskInfo) {
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.TEXT_CLICK_SUCCESS);
    WebsocketDataDTO dataDTO = WebsocketDataDTO.create(taskInfo.getId(), taskInfo.getName(), "????????????", true);
    dataDTO.setState(state);
    dto.setData(dataDTO);
    notice(userId, dto);
  }

  private void notice(Integer userId, WebsocketDTO dto) {
    WebSocketHandler.sendByUserId(userId, dto);
  }

  private void setState(Long taskId, String state) {
    String taskKey = getTextClickTaskKey(taskId);
    stringRedisTemplate.opsForValue().set(taskKey, state, 720, TimeUnit.MINUTES);
  }

  private void handleError(AlgorithmTask task, Exception e, TaskInfoPO infoPO) {
    log.error("[TextOneClickHandler.handleError]", e);
    Integer userId = (Integer) task.getParams().get(SESSION_USER_ID);
    RuleInfoPO po = new RuleInfoPO();
    po.setTaskId(infoPO.getId());
    po.setCompleted(0);
    List<RuleInfoPO> poList = ruleInfoService.selectBySelective(po);
    poList.forEach(ruleInfoPO -> {
      ruleInfoPO.setCompleted(2);
      ruleInfoService.updateByIdSelective(ruleInfoPO);
    });
    WebsocketDTO dto = new WebsocketDTO();
    dto.setEvent(WsEventType.TEXT_CLICK_FAIL);
    String msg = "????????????";
    if (e instanceof BusinessException) {
      msg = e.getMessage();
    }
    String errorMsg = String.format("??????????????????????????????????????????%s??????????????????%s", infoPO.getName(), msg);
    WebsocketDataDTO dataDTO =
        WebsocketDataDTO.create(infoPO.getId(), infoPO.getName(), errorMsg, false);
    dataDTO.setState(STATE_HOLDER.get());
    dto.setData(dataDTO);
    notice(userId, dto);
  }

  private void clearResource(Long taskId) {
    STATE_HOLDER.remove();
    String taskKey = getTextClickTaskKey(taskId);
    stringRedisTemplate.delete(taskKey);
  }

  private void deleteRuleAndLabel(List<Long> ruleIds, Long taskId) {
    for (Long ruleId : ruleIds) {
      // ????????????
      ruleInfoService.deleteRuleById(ruleId);
      // ???????????????????????????
      ruleResultService.deleteByTaskIdAndRuleId(taskId, ruleId);
      // ??????????????????????????????
      ruleUnlabeledResultService.deleteByTaskIdAndRuleId(taskId, ruleId);
    }
  }

  @Autowired
  public void setRuleHandleService(RuleHandleService ruleHandleService) {
    this.ruleHandleService = ruleHandleService;
  }

  @Autowired
  public void setIntegrationHandler(IntegrationSubHandler integrationHandler) {
    this.integrationHandler = integrationHandler;
  }

  @Autowired
  public void setGlobalRuleParameterCalc(GlobalRuleParameterCalc globalRuleParameterCalc) {
    this.globalRuleParameterCalc = globalRuleParameterCalc;
  }

  @Autowired
  public void setDatasetInfoService(DatasetInfoService datasetInfoService) {
    this.datasetInfoService = datasetInfoService;
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setAutoLabelSubHandler(AutoLabelSubHandler autoLabelSubHandler) {
    this.autoLabelSubHandler = autoLabelSubHandler;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setTrainRecordsService(TrainRecordsService trainRecordsService) {
    this.trainRecordsService = trainRecordsService;
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
  public void setModelTrainSubHandler(ModelTrainSubHandler modelTrainSubHandler) {
    this.modelTrainSubHandler = modelTrainSubHandler;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
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
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
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
  public void setIntegrationService(IntegrationService integrationService) {
    this.integrationService = integrationService;
  }

  @Autowired
  public void setFewShotSubHandler(FewShotSubHandler fewShotSubHandler) {
    this.fewShotSubHandler = fewShotSubHandler;
  }

  @Autowired
  public void setTestDataService(TestDataService testDataService) {
    this.testDataService = testDataService;
  }

  @Autowired
  public void setRuleResultService(RuleResultService ruleResultService) {
    this.ruleResultService = ruleResultService;
  }

  @Autowired
  public void setRuleUnlabeledResultService(RuleUnlabeledResultService ruleUnlabeledResultService) {
    this.ruleUnlabeledResultService = ruleUnlabeledResultService;
  }
}
