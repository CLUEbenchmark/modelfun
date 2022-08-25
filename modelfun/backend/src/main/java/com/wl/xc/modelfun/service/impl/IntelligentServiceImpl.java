package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_USER_ID;
import static com.wl.xc.modelfun.commons.enums.AlgorithmTaskType.NER_ONE_CLICK;
import static com.wl.xc.modelfun.commons.enums.AlgorithmTaskType.TEXT_ONE_CLICK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.OneClickCallbackDTO;
import com.wl.xc.modelfun.entities.dto.OneClickCallbackDTO.EntitiesDTO;
import com.wl.xc.modelfun.entities.dto.OneClickCallbackDTO.LabelDTO;
import com.wl.xc.modelfun.entities.dto.OneClickCallbackDTO.ResultsDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.NerAutoLabelMapPO;
import com.wl.xc.modelfun.entities.po.NerAutoLabelResultPO;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.TextOneClickVO;
import com.wl.xc.modelfun.service.DatasetDetailService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.IntelligentService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.NerAutoLabelMapService;
import com.wl.xc.modelfun.service.NerAutoLabelResultService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.algorithm.AlgoTaskAppendEventPublisher;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import com.wl.xc.modelfun.tasks.daemon.OneClickCallbackListener;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/5/26 11:26
 */
@Slf4j
@Service
public class IntelligentServiceImpl implements IntelligentService {

  private TaskInfoService taskInfoService;

  private StringRedisTemplate stringRedisTemplate;

  private DatasetDetailService datasetDetailService;

  private AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher;

  private OneClickCallbackListener oneClickCallbackListener;

  private NerAutoLabelResultService nerAutoLabelResultService;

  private NerAutoLabelMapService autoLabelMapService;

  private LabelInfoService labelInfoService;

  private UnlabelDataService unlabelDataService;

  private IntegrationRecordsService integrationRecordsService;

  private RuleInfoService ruleInfoService;

  private IntegrationServiceImpl integrationService;

  private ObjectMapper objectMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> oneClickTrain(TaskIdReq taskIdReq) {
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    Long taskId = taskIdReq.getTaskId();
    TaskInfoPO taskInfo = taskInfoService.getById(taskId);
    if (taskInfo == null) {
      return ResultVo.create("任务不存在", -1, false, false);
    }
    // 查找任务下的数据集文件
    long count = datasetDetailService.countByTaskId(taskId);
    if (count == 0) {
      return ResultVo.create("请先上传数据集文件", -1, false, false);
    }
    // 判断当前是否有一键标注/训练任务正在进行
    String cacheKey = RedisKeyMethods.getOneClickCacheKey(taskId);
    Boolean exit = stringRedisTemplate.opsForValue().setIfAbsent(cacheKey, userInfo.getUid(), 20, TimeUnit.MINUTES);
    if (Boolean.FALSE.equals(exit)) {
      return ResultVo.create("当前任务正在进行一键标注，请等待之前的任务结束", -1, false, false);
    }
    // 删除原先数据
    nerAutoLabelResultService.deleteByTaskId(taskId);
    autoLabelMapService.deleteByTaskId(taskId);
    // 生成一条集成记录
    count = labelInfoService.countLabelInfoByTaskId(taskId);
    IntegrationRecordsPO po = new IntegrationRecordsPO();
    // 标签数量
    po.setTrainLabelCount((int) count);
    po.setTaskId(taskId);
    po.setIntegrateStatus(0);
    po.setDatasetId(0);
    integrationRecordsService.save(po);
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskId);
    algorithmTask.setRecordId(po.getId());
    algorithmTask.setType(NER_ONE_CLICK);
    Map<String, Object> params = new HashMap<>();
    params.put(SESSION_UID, userInfo.getUid());
    algorithmTask.setParams(params);
    algoTaskAppendEventPublisher.publish(algorithmTask);
    return ResultVo.createSuccess(true);
  }


  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> oneClickCallback(OneClickCallbackDTO dto) {
    log.info(
        "[IntelligentServiceImpl.oneClickCallback] 收到一键标注回调，taskId={}, state={}, detail={}",
        dto.getTaskId(), dto.getState(), dto.getDetail());
    ResultsDTO results = dto.getResults();
    if (Boolean.TRUE.equals(dto.getState()) && results != null) {
      log.info("[IntelligentServiceImpl.oneClickCallback] precision={}, recall={}, f1={}",
          results.getPrecision(), results.getRecall(), results.getFscore());
    }
    Long taskId = dto.getTaskId();
    TaskInfoPO taskInfo = taskInfoService.getById(taskId);
    if (taskInfo == null) {
      return ResultVo.create("任务不存在", -1, false, false);
    }
    oneClickCallbackListener.removeCallbackListener(taskId);
    String cacheKey = RedisKeyMethods.getOneClickCacheKey(taskId);
    String uid = stringRedisTemplate.opsForValue().get(cacheKey);
    IntegrationRecordsPO integrationRecordsPO = integrationRecordsService.getById(dto.getRecordId());
    if (integrationRecordsPO == null) {
      sendWebsocket(uid, taskInfo, "一键标注失败，请重试", WsEventType.ONE_CLICK_FAIL, false);
      stringRedisTemplate.delete(cacheKey);
      return ResultVo.create("记录不存在", -1, false, false);
    }
    Boolean state = dto.getState();
    if (Boolean.FALSE.equals(state) || results == null) {
      // 如果任务失败，提醒用户
      String detail = dto.getDetail();
      if (StringUtils.isBlank(detail)) {
        detail = "任务失败";
      }
      sendWebsocket(uid, taskInfo, detail, WsEventType.ONE_CLICK_FAIL, false);
      stringRedisTemplate.delete(cacheKey);
      integrationRecordsPO.setLabeled(2);
      integrationRecordsPO.setIntegrateStatus(2);
      integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsService.updateById(integrationRecordsPO);
      return ResultVo.createSuccess(true);
    }
    try {
      integrationRecordsPO.setIntegrateStatus(1);
      integrationRecordsPO.setLabeled(2);
      integrationRecordsPO.setTestF1Score(CalcUtil.multiply(results.getFscore() + "", "100", 2));
      // 这里把精准率的结果存到准确率的字段中，因为不想再多加字段
      integrationRecordsPO.setTestAccuracy(CalcUtil.multiply(results.getPrecision() + "", "100", 2));
      integrationRecordsPO.setTestRecall(CalcUtil.multiply(results.getRecall() + "", "100", 2));
      LocalDateTime createDatetime = integrationRecordsPO.getUpdateDatetime();
      LocalDateTime now = LocalDateTime.now();
      integrationRecordsPO.setTimeCost((int) createDatetime.until(now, ChronoUnit.SECONDS));
      // 保存一键标注结果
      List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(dto.getTaskId());
      Map<String, Integer> map = labelInfoPOS.stream()
          .collect(Collectors.toMap(LabelInfoPO::getLabelDesc, LabelInfoPO::getLabelId));
      List<LabelDTO> labelDTOS = results.getLabel();
      int size = labelDTOS.size();
      List<NerAutoLabelResultPO> nerAutoLabelResultPOS = new ArrayList<>(size);
      List<NerAutoLabelMapPO> nerAutoLabelMapPOS = new ArrayList<>(size);
      for (int j = 0; j < size; j++) {
        LabelDTO model = labelDTOS.get(j);
        List<EntitiesDTO> entities = model.getEntities();
        if (entities == null || entities.isEmpty()) {
          continue;
        } else if (entities.size() == 1 && entities.get(0).getId() == null) {
          continue;
        }
        NerAutoLabelResultPO po = new NerAutoLabelResultPO();
        po.setSentenceId((long) (j + 1));
        po.setTaskId(taskId);
        nerAutoLabelResultPOS.add(po);
        for (EntitiesDTO entity : entities) {
          if (!map.containsKey(entity.getLabel())) {
            continue;
          }
          NerAutoLabelMapPO labelMapPO = new NerAutoLabelMapPO();
          labelMapPO.setLabelId(map.get(entity.getLabel()));
          labelMapPO.setSentenceId((long) (j + 1));
          labelMapPO.setTaskId(taskId);
          labelMapPO.setDataId(entity.getId() == null ? null : (long) entity.getId());
          labelMapPO.setStartOffset(entity.getStartOffset());
          labelMapPO.setEndOffset(entity.getEndOffset());
          nerAutoLabelMapPOS.add(labelMapPO);
        }
      }
      // 简单点，实现的方式简单点
      List<Long> collect = nerAutoLabelResultPOS.stream().map(NerAutoLabelResultPO::getSentenceId)
          .collect(Collectors.toList());
      List<UnlabelDataPO> dataPOList = unlabelDataService.listByDataId(taskId, collect);
      Map<Long, UnlabelDataPO> dataMap = dataPOList.stream()
          .collect(Collectors.toMap(UnlabelDataPO::getDataId, Function.identity()));
      for (NerAutoLabelResultPO po : nerAutoLabelResultPOS) {
        UnlabelDataPO dataPO = dataMap.get(po.getSentenceId());
        po.setSentence(dataPO.getSentence());
      }
      integrationRecordsPO.setTrainSentenceCount((long) nerAutoLabelResultPOS.size());
      // 未标注集的覆盖率 = 标注数据 / 总数据
      integrationRecordsPO.setUnlabelCoverage(
          CalcUtil.multiply(CalcUtil.divide(nerAutoLabelResultPOS.size(), size, 2), "100"));
      nerAutoLabelResultService.saveForBatchNoLog(nerAutoLabelResultPOS);
      autoLabelMapService.saveForBatchNoLog(nerAutoLabelMapPOS);
      integrationRecordsPO.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsService.updateById(integrationRecordsPO);
      sendWebsocket(uid, taskInfo, "一键标注完成", WsEventType.ONE_CLICK_SUCCESS, true);
    } finally {
      stringRedisTemplate.delete(cacheKey);
    }
    return ResultVo.createSuccess(true);
  }

  @Override
  public ResultVo<TextOneClickVO> existTextOneClick(TaskIdReq taskIdReq) {
    String taskKey = RedisKeyMethods.getTextClickTaskKey(taskIdReq.getTaskId());
    String s = stringRedisTemplate.opsForValue().get(taskKey);
    TextOneClickVO vo = new TextOneClickVO();
    vo.setExist(false);
    if (!StringUtils.isBlank(s)) {
      vo.setExist(true);
      vo.setState(Integer.parseInt(s));
    }
    return ResultVo.createSuccess(vo);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public ResultVo<Boolean> textOneClickTrain(TaskIdReq taskIdReq) {
    String taskKey = RedisKeyMethods.getTextClickTaskKey(taskIdReq.getTaskId());
    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(taskKey))) {
      return ResultVo.create("当前正有一键标注任务在进行中，请稍后再试", -1, false, false);
    }
    // 判断是否有规则正在运行或者运行失败
    List<RuleInfoPO> ruleList = ruleInfoService.getRuleListByTaskId(taskIdReq.getTaskId());
    if (ruleList != null && !ruleList.isEmpty()) {
      Map<Integer, List<RuleInfoPO>> map = ruleList.stream().collect(Collectors.groupingBy(RuleInfoPO::getCompleted));
      List<RuleInfoPO> running = map.get(0);
      List<RuleInfoPO> failed = map.get(2);
      List<RuleInfoPO> deleting = map.get(3);
      StringBuilder sb = new StringBuilder("当前有");
      if (running != null && !running.isEmpty()) {
        sb.append(running.size()).append("条规则正在运行。");
      }
      if (deleting != null && !deleting.isEmpty()) {
        sb.append(deleting.size()).append("条规则正在删除。");
      }
      if (sb.length() > 3) {
        sb.append("请稍后再试。");
        return ResultVo.create(sb.toString(), -1, false, false);
      }
    }
    ResultVo<Boolean> resultVo = integrationService.existRunningIntegration(taskIdReq.getTaskId());
    if (resultVo.getData()) {
      return ResultVo.create("当前有集成任务正在运行，请稍后再试", -1, false, false);
    }
    // 判断是否有标注任务正在运行
    resultVo = integrationService.existLabelingTask(taskIdReq.getTaskId());
    if (resultVo.getData()) {
      return ResultVo.create("当前有标注任务正在运行，请稍后再试", -1, false, false);
    }
    AlgorithmTask algorithmTask = new AlgorithmTask();
    algorithmTask.setTaskId(taskIdReq.getTaskId());
    algorithmTask.setType(TEXT_ONE_CLICK);
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    Map<String, Object> params = new HashMap<>();
    params.put(SESSION_UID, userInfo.getUid());
    params.put(SESSION_USER_ID, userInfo.getUserId());
    algorithmTask.setParams(params);
    algoTaskAppendEventPublisher.publish(algorithmTask);
    return ResultVo.createSuccess(true);
  }

  private void sendWebsocket(String uid, TaskInfoPO taskInfo, String detail, WsEventType type, boolean success) {
    WebsocketDTO websocketDTO = new WebsocketDTO();
    websocketDTO.setEvent(type);
    websocketDTO.setData(WebsocketDataDTO.create(taskInfo.getId(), taskInfo.getName(), detail, success));
    WebSocketHandler.sendByUid(uid, websocketDTO);
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
  public void setDatasetDetailService(DatasetDetailService datasetDetailService) {
    this.datasetDetailService = datasetDetailService;
  }

  @Autowired
  public void setAlgoTaskAppendEventPublisher(
      AlgoTaskAppendEventPublisher algoTaskAppendEventPublisher) {
    this.algoTaskAppendEventPublisher = algoTaskAppendEventPublisher;
  }

  @Autowired
  public void setOneClickCallbackListener(OneClickCallbackListener oneClickCallbackListener) {
    this.oneClickCallbackListener = oneClickCallbackListener;
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
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setIntegrationService(IntegrationServiceImpl integrationService) {
    this.integrationService = integrationService;
  }
}
