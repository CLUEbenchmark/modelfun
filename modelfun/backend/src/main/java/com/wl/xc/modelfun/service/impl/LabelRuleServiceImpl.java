package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.RULE_DELETE_FAIL;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.RULE_NOT_EXIST;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.RULE_TYPE_NOT_EXIST;
import static com.wl.xc.modelfun.commons.enums.ResponseCodeEnum.RULE_UPDATE_FAIL;

import cn.hutool.core.text.UnicodeUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.RequestConfigHolder;
import com.wl.xc.modelfun.commons.enums.BuiltinModelType;
import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessArgumentException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.config.properties.AlgorithmProperties;
import com.wl.xc.modelfun.config.properties.CommonProperties;
import com.wl.xc.modelfun.entities.dto.GPTCallbackDTO;
import com.wl.xc.modelfun.entities.dto.GPTTestRspDTO;
import com.wl.xc.modelfun.entities.dto.GptDTO;
import com.wl.xc.modelfun.entities.dto.LabelFunctionDTO;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule.ExampleDTO;
import com.wl.xc.modelfun.entities.model.DatabaseRule;
import com.wl.xc.modelfun.entities.model.ExpertRule;
import com.wl.xc.modelfun.entities.model.LabelFunctionRule;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.model.OpenApiReq;
import com.wl.xc.modelfun.entities.model.OpenApiResponse;
import com.wl.xc.modelfun.entities.model.OpenApiResponse.DataDTO;
import com.wl.xc.modelfun.entities.model.OpenApiRule;
import com.wl.xc.modelfun.entities.model.RegexRule;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.RuleOverviewPO;
import com.wl.xc.modelfun.entities.po.RuleResultPO;
import com.wl.xc.modelfun.entities.po.TaskExpertPO;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import com.wl.xc.modelfun.entities.req.LabelFunctionTestReq;
import com.wl.xc.modelfun.entities.req.LabelPageReq;
import com.wl.xc.modelfun.entities.req.RegexTestReq;
import com.wl.xc.modelfun.entities.req.RuleDataReq;
import com.wl.xc.modelfun.entities.req.RuleOpReq;
import com.wl.xc.modelfun.entities.req.RuleResultReq;
import com.wl.xc.modelfun.entities.req.TaskIdPageReq;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.DatasetInfoVO;
import com.wl.xc.modelfun.entities.vo.DictKeyValueVO;
import com.wl.xc.modelfun.entities.vo.ExpertVO;
import com.wl.xc.modelfun.entities.vo.LabelHFWordVO;
import com.wl.xc.modelfun.entities.vo.LabelRuleVO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ParseTaskVO;
import com.wl.xc.modelfun.entities.vo.RegexMatchDataVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.RuleMistakeVO;
import com.wl.xc.modelfun.entities.vo.RuleOverviewVO;
import com.wl.xc.modelfun.service.DatasetInfoService;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.service.LabelRuleService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.RuleOverviewService;
import com.wl.xc.modelfun.service.RuleResultService;
import com.wl.xc.modelfun.service.RuleUnlabeledResultService;
import com.wl.xc.modelfun.service.TaskExpertService;
import com.wl.xc.modelfun.service.UnlabelDataService;
import com.wl.xc.modelfun.tasks.rule.RuleTaskAppendEventPublisher;
import com.wl.xc.modelfun.tasks.rule.handlers.RegexHandler;
import com.wl.xc.modelfun.utils.BeanCopyUtil;
import com.wl.xc.modelfun.utils.CalcUtil;
import com.wl.xc.modelfun.utils.PythonCheckUtil;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * 标签规则服务实现类
 *
 * @version 1.0
 * @date 2022/4/12 9:54
 */
@Slf4j
@Service
public class LabelRuleServiceImpl implements LabelRuleService {

  private RuleInfoService ruleInfoService;

  private LabelInfoService labelInfoService;

  private DatasetInfoService datasetInfoService;

  private TaskExpertService taskExpertService;

  private RuleOverviewService ruleOverviewService;

  private RuleResultService ruleResultService;

  private RuleUnlabeledResultService ruleUnlabeledResultService;

  private RuleTaskAppendEventPublisher publisher;

  private ObjectMapper objectMapper;

  private StringRedisTemplate stringRedisTemplate;

  private RestTemplate restTemplate;

  private UnlabelDataService unlabelDataService;

  private AlgorithmProperties algorithmProperties;

  private CommonProperties commonProperties;

  @Override
  public ResultVo<List<LabelRuleVO>> getRuleList(Long taskId) {
    List<RuleInfoPO> ruleList = ruleInfoService.getRuleListByTaskId(taskId);
    List<LabelRuleVO> result = ruleList.stream().map(rule -> {
      LabelRuleVO labelRuleVO = new LabelRuleVO();
      BeanCopyUtil.copy(rule, labelRuleVO);
      return labelRuleVO;
    }).collect(Collectors.toList());
    return ResultVo.createSuccess(result);
  }

  @Override
  public PageVO<DictKeyValueVO> getLabelPageByTaskId(LabelPageReq req) {
    Page<LabelInfoPO> page = Page.of(req.getCurPage(), req.getPageSize());
    LabelInfoPO po = new LabelInfoPO();
    po.setTaskId(req.getTaskId());
    po.setLabelDesc(req.getLabelDesc());
    PageVO<LabelInfoPO> pageVO = labelInfoService.pageLabelInfo(page, po);
    return pageVO.convert(l -> {
      DictKeyValueVO vo = new DictKeyValueVO();
      vo.setMapKey(l.getLabelId().toString());
      vo.setMapValue(l.getLabelDesc());
      vo.setMapSort(l.getLabelId());
      return vo;
    });
  }

  @Override
  public ResultVo<Boolean> addRule(RuleOpReq req) {
    // 校验规则准确性
    validRuleContext(req);
    RuleInfoPO ruleInfoPO = new RuleInfoPO();
    DatasetInfoPO lastDatasetInfo = datasetInfoService.getLastDatasetInfo(req.getTaskId());
    if (lastDatasetInfo != null) {
      ruleInfoPO.setDatasetId(lastDatasetInfo.getId());
    }
    BeanCopyUtil.copy(req, ruleInfoPO);
    ruleInfoService.save(ruleInfoPO);
    // 添加完成规则之后，需要提交规则任务
    LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
    HashMap<String, Object> map = new HashMap<>();
    map.put(SESSION_UID, userInfo.getUid());
    publisher.publish(req.getTaskId(), ruleInfoPO, RuleTaskType.SINGLE, map);
    return ResultVo.createSuccess(true);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ResultVo<Boolean> deleteRule(RuleOpReq req) {
    // 删除规则
    RuleInfoPO ruleInfoPO = ruleInfoService.getById(req.getRuleId());
    if (ruleInfoPO == null) {
      return ResultVo.create(RULE_NOT_EXIST, false, false);
    }
    RuleInfoPO ruleInfoPO2 = new RuleInfoPO();
    ruleInfoPO2.setCompleted(3);
    ruleInfoPO2.setId(ruleInfoPO.getId());
    boolean result = ruleInfoService.updateById(ruleInfoPO2);
    if (!result) {
      return ResultVo.create(RULE_DELETE_FAIL, false, false);
    }
    // 删除规则下的运行结果
    removeRuleResult(ruleInfoPO);
    return ResultVo.createSuccess(true);
  }

  @Override
  public ResultVo<Boolean> updateRule(RuleOpReq req) {
    RuleInfoPO oldRuleInfo = ruleInfoService.getById(req.getRuleId());
    String metadata = req.getMetadata();
    boolean needReRun = true;
    if ((StringUtils.isBlank(metadata)
        || oldRuleInfo.getMetadata().equals(metadata)) && oldRuleInfo.getCompleted() != 2) {
      // 单纯改了规则信息，没有改规则元数据，不需要重新跑任务
      needReRun = false;
    } else {
      // 改了规则元数据，需要重新跑任务,并且对GPT3进行appkey判断
      int ruleType = req.getRuleType() == null ? oldRuleInfo.getRuleType() : req.getRuleType();
      if (ruleType == RuleType.GPT3.getType()) {
        BuiltinModelRule r;
        try {
          r = objectMapper.readValue(metadata, BuiltinModelRule.class);
        } catch (JsonProcessingException e) {
          throw new BusinessIllegalStateException("规则格式错误，请检查规则！", e);
        }
        if (r.getModelName() == BuiltinModelType.GPT3.getType()
            && !algorithmProperties.getGptKey().equals(r.getAppKey())) {
          return ResultVo.create("请输入正确的appkey", -1, false, false);
        }
        checkBuiltinRule(req.getTaskId(), req.getRuleId(), r);
      }
    }
    RuleInfoPO ruleInfoPO = new RuleInfoPO();
    BeanCopyUtil.copy(req, ruleInfoPO);
    ruleInfoPO.setId(req.getRuleId());
    if (needReRun) {
      ruleInfoPO.setCompleted(0);
    } else {
      ruleInfoPO.setCompleted(null);
    }
    ruleInfoPO.setUpdateDatetime(LocalDateTime.now());
    boolean result = ruleInfoService.updateByIdSelective(ruleInfoPO) > 0;
    if (result) {
      // 更新完成规则之后，需要提交规则任务
      ruleInfoPO = ruleInfoService.getById(req.getRuleId());
      if (!needReRun) {
        return ResultVo.createSuccess(true);
      }
      LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
      HashMap<String, Object> map = new HashMap<>();
      map.put(SESSION_UID, userInfo.getUid());
      publisher.publish(req.getTaskId(), ruleInfoPO, RuleTaskType.SINGLE, map);
    }
    return result ? ResultVo.createSuccess(true) : ResultVo.create(RULE_UPDATE_FAIL, false, false);
  }

  @Override
  public ResultVo<List<ExpertVO>> getTaskExpertList(Long taskId) {
    List<TaskExpertPO> expertPOList = taskExpertService.getAllByTaskId(taskId);
    List<ExpertVO> result =
        expertPOList.stream()
            .map(
                taskExpertPO -> {
                  ExpertVO vo = new ExpertVO();
                  vo.setId(taskExpertPO.getId());
                  vo.setFileName(taskExpertPO.getFileName());
                  vo.setAddress(taskExpertPO.getFileAddress());
                  return vo;
                })
            .collect(Collectors.toList());
    return ResultVo.createSuccess(result);
  }

  @Override
  public ResultVo<RuleOverviewVO> getRuleOverview(Long taskId) {
    RuleOverviewVO ruleOverviewVO = new RuleOverviewVO();
    long count = labelInfoService.countLabelInfoByTaskId(taskId);
    RuleOverviewPO ruleOverviewPO = ruleOverviewService.getRuleOverviewByTaskId(taskId);
    ruleOverviewVO.setLabelCount(count);
    if (ruleOverviewPO == null) {
      return ResultVo.createSuccess(ruleOverviewVO);
    }
    ruleOverviewVO.setAccuracy(ruleOverviewPO.getAccuracy());
    ruleOverviewVO.setCoverage(ruleOverviewPO.getCoverage());
    ruleOverviewVO.setConflict(ruleOverviewPO.getConflict());
    ruleOverviewVO.setTestDataCoverage(ruleOverviewPO.getTestDataCoverage());
    String expertKey = RedisKeyMethods.generateExpertKey(taskId);
    String s = stringRedisTemplate.opsForValue().get(expertKey);
    if (StringUtils.isBlank(s)) {
      ruleOverviewVO.setExitParseTask(false);
    } else {
      ruleOverviewVO.setExitParseTask(true);
      ruleOverviewVO.setRequestId(s);
    }
    return ResultVo.createSuccess(ruleOverviewVO);
  }

  @Override
  public ResultVo<String> labelFunctionTest(LabelFunctionTestReq req) {
    LabelFunctionDTO dto = new LabelFunctionDTO();
    dto.setName(req.getFunctionName());
    dto.setContent(PythonCheckUtil.reWriteBody(req.getFunctionBody()));
    dto.setLanguage("python");
    dto.setDataSample(req.getExample());
    if (dto.getName().equals("lf")) {
      dto.setName("label_function_test");
    }
    try {
      ResponseEntity<String> response =
          restTemplate.postForEntity(
              algorithmProperties.getLabelFunctionTestPath(), dto, String.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        return ResultVo.createSuccess(response.getBody());
      } else {
        return ResultVo.create("测试失败，请检查代码格式！", -1, false, null);
      }
    } catch (InternalServerError ex) {
      String asString = ex.getResponseBodyAsString();
      JsonNode jsonNode;
      try {
        jsonNode = objectMapper.readTree(asString);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("服务器繁忙，请稍后重试！");
      }
      return ResultVo.create("", 0, true, jsonNode.get("detail").asText());
    } catch (Exception e) {
      throw new BusinessIllegalStateException("服务器繁忙，请稍后重试！", 500, e);
    }
  }

  @Override
  public ResultVo<Boolean> getRunningRuleByTaskId(Long taskId) {
    Long count = ruleInfoService.countRunningRule(taskId);
    return count > 0 ? ResultVo.createSuccess(true) : ResultVo.createSuccess(false);
  }

  @Override
  public PageVO<RuleMistakeVO> getMistakeList(RuleResultReq req) {
    Page<RuleMistakeVO> page = Page.of(req.getCurPage(), req.getPageSize());
    RuleResultPO po = new RuleResultPO();
    po.setRuleId(req.getRuleId());
    po.setTaskId(req.getTaskId());
    return ruleResultService.getMistakeByTaskIdAndRule(po, page);
  }

  @Override
  public PageVO<RuleMistakeVO> getUnCoverageList(RuleDataReq req) {
    Page<RuleMistakeVO> page = Page.of(req.getCurPage(), req.getPageSize());
    RuleResultPO po = new RuleResultPO();
    po.setTaskId(req.getTaskId());
    po.setRuleId(req.getRuleId());
    if (RuleType.REGEX.getType().equals(req.getRuleType())) {
      po.setLabelId(req.getLabel());
    }
    return ruleResultService.getUnCoverageByTaskIdAndRule(po, page);
  }

  @Override
  public PageVO<LabelHFWordVO> getLabelHfWord(TaskIdPageReq req) {
    IPage<LabelInfoPO> page = new Page<>(req.getCurPage(), req.getPageSize());
    LabelInfoPO labelInfoPO = new LabelInfoPO();
    labelInfoPO.setTaskId(req.getTaskId());
    PageVO<LabelInfoPO> vo = labelInfoService.pageLabelInfo(page, labelInfoPO);
    return vo.convert(this::convertLabelHfWord);
  }

  @Override
  public ResultVo<ParseTaskVO> isExistExpertTask(TaskIdReq req) {
    String expertKey = RedisKeyMethods.generateExpertKey(req.getTaskId());
    String s = stringRedisTemplate.opsForValue().get(expertKey);
    ParseTaskVO vo = new ParseTaskVO();
    if (StringUtils.isBlank(s)) {
      vo.setExitParseTask(false);
    } else {
      vo.setExitParseTask(true);
      vo.setRequestId(s);
    }
    return ResultVo.createSuccess(vo);
  }

  @Override
  public ResultVo<Boolean> saveGPTResultAsync(GPTCallbackDTO callbackDTO) {
    String cacheKey = RedisKeyMethods.getGPTCacheKey(callbackDTO.getTaskId(), callbackDTO.getRecordId());
    String result;
    try {
      result = objectMapper.writeValueAsString(callbackDTO);
    } catch (JsonProcessingException e) {
      log.error("[LabelRuleServiceImpl.saveGPTResultAsync]", e);
      stringRedisTemplate.opsForValue().set(cacheKey, "", 10, TimeUnit.MINUTES);
      return ResultVo.create("标注结果为空，请检查格式！", -1, false, null);
    }
    stringRedisTemplate.opsForValue().set(cacheKey, result, 10, TimeUnit.MINUTES);
    return ResultVo.createSuccess(true);
  }

  @Override
  public ResultVo<String> gptLfTest(BuiltinModelRule req) {
    BuiltinModelType type = BuiltinModelType.getFromType(req.getModelName());
    if (type == BuiltinModelType.GPT3 && !algorithmProperties.getGptKey().equals(req.getAppKey())) {
      return ResultVo.create("请输入正确的appkey！", -1, false, null);
    }
    GptDTO gptDTO = new GptDTO();
    gptDTO.setTexts(req.getTexts());
    List<ExampleDTO> exampleDTOS = req.getExample();
    List<List<String>> example = new ArrayList<>(exampleDTOS.size());
    for (ExampleDTO exampleDTO : exampleDTOS) {
      List<String> list = new ArrayList<>(2);
      list.add(exampleDTO.getSentence());
      list.add(exampleDTO.getLabelDes());
      example.add(list);
    }
    gptDTO.setExamples(example);
    List<LabelInfoPO> labelInfoPOS = labelInfoService.selectListByTaskId(req.getTaskId());
    Map<Integer, String> map = labelInfoPOS.stream()
        .collect(Collectors.toMap(LabelInfoPO::getLabelId, LabelInfoPO::getLabelDesc));
    List<String> labels = new ArrayList<>(labelInfoPOS.size());
    for (Integer label : req.getLabels()) {
      String s = map.get(label);
      if (s != null) {
        labels.add(s);
      }
    }
    gptDTO.setLabels(labels);
    gptDTO.setModelName(type.getName());
    // 超时时间一分钟
    RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(60 * 1000).build();
    RequestConfigHolder.bind(requestConfig);
    try {
      log.info("[LabelRuleServiceImpl.gptLfTest] 发送GPT3测试请求");
      ResponseEntity<GPTTestRspDTO> responseEntity =
          restTemplate.postForEntity(
              algorithmProperties.getGptTestUrl(), gptDTO, GPTTestRspDTO.class);
      GPTTestRspDTO result = responseEntity.getBody();
      if (result == null || StringUtils.isBlank(result.getLabels())) {
        return ResultVo.create("当前服务器资源紧张，请稍后再试！", -1, false, null);
      }
      log.info("[LabelRuleServiceImpl.gptLfTest] 测试结果：{}", result.getLabels());
      return ResultVo.createSuccess(UnicodeUtil.toString(result.getLabels()));
    } catch (RestClientResponseException responseException) {
      throw new BusinessIllegalStateException(responseException.getMessage(), responseException);
    } catch (Exception e) {
      return ResultVo.create("当前服务器资源紧张，请稍后再试！", -1, false, null);
    } finally {
      RequestConfigHolder.clear();
    }
  }

  @Override
  public ResultVo<String> regexTest(RegexTestReq req) {
    RegexHandler handler = new RegexHandler(req.getRules(), 1);
    handler.init();
    int label = handler.label(req.getText(), DatasetType.TEST);
    handler.destroy();
    if (label == 1) {
      return ResultVo.createSuccess("匹配成功！");
    } else {
      return ResultVo.createSuccess("匹配失败！");
    }
  }

  @Override
  public ResultVo<String> openApiTest(OpenApiRule req) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    OpenApiReq openApiReq = new OpenApiReq();
    openApiReq.setSentences(Collections.singletonList(req.getRequestBody()));
    HttpEntity<OpenApiReq> reqHttpEntity = new HttpEntity<>(openApiReq, headers);
    ResponseEntity<String> response;
    try {
      response = restTemplate.postForEntity(req.getHost(),
          reqHttpEntity, String.class);
    } catch (RestClientResponseException e) {
      log.error("[LabelRuleServiceImpl.openApiTest] 调用外部接口失败", e);
      String bodyAsString = e.getResponseBodyAsString();
      return ResultVo.createSuccess(bodyAsString);
    } catch (Exception e) {
      log.error("[LabelRuleServiceImpl.openApiTest] 调用外部接口失败", e);
      return ResultVo.createSuccess("当前服务器资源紧张，请稍后再试！");
    }
    String body = response.getBody();
    if (body == null) {
      return ResultVo.createSuccess("调用外部系统接口失败，接口返回为空");
    }
    OpenApiResponse openApiResp;
    try {
      openApiResp = objectMapper.readValue(body, OpenApiResponse.class);
    } catch (JsonProcessingException e) {
      return ResultVo.createSuccess("调用外部系统接口失败，接口返回格式错误");

    }
    if (openApiResp.getCode() != 0) {
      return ResultVo.createSuccess(openApiResp.getMsg());
    }
    List<DataDTO> data = openApiResp.getData();
    if (data == null) {
      return ResultVo.createSuccess("接口返回为空");
    }
    List<LabelInfoPO> infoPOS = labelInfoService.selectListByTaskId(req.getTaskId());
    Map<Integer, String> labelMap = infoPOS.stream()
        .collect(Collectors.toMap(LabelInfoPO::getLabelId, LabelInfoPO::getLabelDesc));
    Map<String, Integer> map = data.stream().collect(Collectors.toMap(DataDTO::getSentence, DataDTO::getLabelId));
    Integer id = map.get(req.getRequestBody());
    String s = labelMap.get(id);
    return ResultVo.createSuccess(s == null ? "接口未返回语料对应的标签" : s);
  }

  @Override
  public ResultVo<RegexMatchDataVO> getRegexMatchData(RuleDataReq req) {
    String metadata = req.getMetadata();
    List<List<RegexRule>> lists;
    try {
      lists = objectMapper.readValue(metadata, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new BusinessArgumentException("错误的规则参数", e);
    }
    RegexMatchDataVO vo = new RegexMatchDataVO();
    vo.setDataList(Collections.emptyList());
    vo.setCoverage("0%");
    if (lists.isEmpty()) {
      return ResultVo.createSuccess(vo);
    }
    for (List<RegexRule> list : lists) {
      list.removeIf(l -> {
        if (l.getRuleType() == 0) {
          return StringUtils.isBlank(l.getKeyword());
        } else {
          return StringUtils.isBlank(l.getRegex());
        }
      });
    }
    lists.removeIf(List::isEmpty);
    // 从数据库随机获取部分数据
    long count = unlabelDataService.countUnlabelDataByTaskId(req.getTaskId());
    Long maxId = unlabelDataService.getMaxDataIdByTaskId(req.getTaskId());
    if (count == 0) {
      return ResultVo.createSuccess(vo);
    }
    count = Math.min(count, commonProperties.getUnlabelSize());
    count = Math.min(count, maxId);
    Random random = new SecureRandom();
    ;
    Set<Long> randomIndex =
        random
            .longs(0, maxId + 1)
            .distinct()
            .limit(count)
            .boxed()
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    List<UnlabelDataPO> unlabelDataPOList = unlabelDataService.listByDataId(req.getTaskId(), randomIndex);
    if (unlabelDataPOList.isEmpty()) {
      return ResultVo.createSuccess(vo);
    }
    RegexHandler handler = new RegexHandler(lists, 1);
    handler.init();
    // 进行计算打标
    int labeledCount = 0;
    int size = unlabelDataPOList.size();
    List<DatasetInfoVO> result = new ArrayList<>(size);
    for (UnlabelDataPO unlabelDataPO : unlabelDataPOList) {
      int label = handler.label(unlabelDataPO.getSentence(), DatasetType.UNLABELED);
      DatasetInfoVO infoVO = new DatasetInfoVO();
      infoVO.setSentence(unlabelDataPO.getSentence());
      if (label != -1) {
        labeledCount++;
        infoVO.setFlag(1);
      } else {
        infoVO.setFlag(0);
      }
      result.add(infoVO);
    }
    String coverage = CalcUtil.multiply(CalcUtil.divide(labeledCount, size, 4), "100", 2);
    vo.setCoverage(coverage + "%");
    vo.setDataList(result);
    return ResultVo.createSuccess(vo);
  }

  private LabelHFWordVO convertLabelHfWord(LabelInfoPO labelInfoPO) {
    LabelHFWordVO vo = new LabelHFWordVO();
    vo.setLabelId(labelInfoPO.getLabelId());
    vo.setLabelDes(labelInfoPO.getLabelDesc());
    vo.setHfWord(labelInfoPO.getHfWord());
    return vo;
  }

  /**
   * 检查规则是否符合规范.
   * <p>
   * 临时性写法，其实应该用类似策略模式进行重写，但是时间有限，用if-else吧。
   *
   * @param req 规则请求
   * @throws BusinessIllegalStateException 规则不符合规范时抛出异常
   */
  private void validRuleContext(RuleOpReq req) {
    String metadata = req.getMetadata();
    if (StringUtils.isBlank(metadata)) {
      throw new BusinessIllegalStateException("规则内容不能为空！", 1003);
    }
    RuleType ruleType = RuleType.getByType(req.getRuleType());
    if (ruleType == null) {
      throw new BusinessIllegalStateException("规则类型不存在", RULE_TYPE_NOT_EXIST.getCode());
    }
    if (ruleType == RuleType.REGEX) {
      if (req.getLabel() == null || StringUtils.isBlank(req.getLabelDes())) {
        throw new BusinessIllegalStateException("模式匹配必须指定输出标签", -1);
      }
    } else if (ruleType == RuleType.EXPERT) {
      List<ExpertRule> expertList;
      try {
        expertList = objectMapper.readValue(metadata, new TypeReference<>() {
        });
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("专家知识规则格式错误", -1);
      }
      if (expertList.isEmpty()) {
        throw new BusinessIllegalStateException("专家知识规则必须指定专家文件", -1);
      }
    } else if (ruleType == RuleType.DATABASE) {
      DatabaseRule databaseRule;
      try {
        databaseRule = objectMapper.readValue(metadata, DatabaseRule.class);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("数据库规则格式错误", -1);
      }
      validDataBaseRule(databaseRule);
    } else if (ruleType == RuleType.OPEN_API) {
      try {
        OpenApiRule openApiRule = objectMapper.readValue(metadata, OpenApiRule.class);
        validOpenApiRule(openApiRule);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("外部系统规则格式错误", -1);
      }
    } else if (ruleType == RuleType.LABEL_FUNCTION) {
      try {
        LabelFunctionRule labelFunctionRule = objectMapper.readValue(metadata, LabelFunctionRule.class);
        validLabelFunctionRule(req.getTaskId(), labelFunctionRule);
        req.setMetadata(objectMapper.writeValueAsString(labelFunctionRule));
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("编写函数规则格式错误", -1);
      }
    } else if (ruleType == RuleType.GPT3) {
      Long count = ruleInfoService.countRunningRuleByType(RuleType.GPT3.getType());
      if (count > 6) {
        throw new BusinessIllegalStateException("当前资源紧张，请稍后再建立内置模型规则", -1);
      }
      BuiltinModelRule modelRule;
      try {
        modelRule = objectMapper.readValue(metadata, BuiltinModelRule.class);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("内置模型规则格式错误", -1);
      }
      Integer modelName = modelRule.getModelName();
      if (BuiltinModelType.GPT3.getType() == modelName) {
        if (!algorithmProperties.getGptKey().equals(modelRule.getAppKey())) {
          throw new BusinessIllegalStateException("请输入正确的appkey", -1);
        }
      }
      if (modelRule.getExample() == null || modelRule.getExample().isEmpty()) {
        throw new BusinessIllegalStateException("标签说明示例不能为空", -1);
      }
      if (modelRule.getLabels() == null || modelRule.getLabels().isEmpty()) {
        throw new BusinessIllegalStateException("标签类型不能为空", -1);
      }
      checkBuiltinRule(req.getTaskId(), null, modelRule);
    }
  }

  /**
   * 判断当前内置模型的label和类型是否完全一致，如果一致则不允许重建
   *
   * @param taskId 任务ID
   * @param ruleId 规则ID
   */
  private void checkBuiltinRule(Long taskId, Long ruleId, BuiltinModelRule modelRule) {
    List<RuleInfoPO> list = ruleInfoService.getRuleListByTaskIdAndType(taskId,
        RuleType.GPT3.getType());
    if (ruleId != null) {
      list.removeIf(ruleInfoPO -> ruleInfoPO.getId().equals(ruleId));
    }
    list.stream().map(r -> {
      String metadata = r.getMetadata();
      try {
        return objectMapper.readValue(metadata, BuiltinModelRule.class);
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("服务器内部错误，请联系管理员！", e);
      }
    }).forEach(r -> {
      if (r.getModelName() == BuiltinModelType.GPT3.getType()
          && modelRule.getModelName().equals(r.getModelName())) {
        throw new BusinessIllegalStateException("只能创建一个全能模型！", -1);
      }
      /*Set<Integer> labels = new HashSet<>(r.getLabels());
      List<Integer> currentLabels = modelRule.getLabels();
      if (modelRule.getModelName().equals(r.getModelName())) {
        if (labels.size() == currentLabels.size()) {
          if (labels.containsAll(currentLabels)) {
            throw new BusinessIllegalStateException("该内置模型内容已经存在，请勿重复添加", -1);
          }
        }
      }*/
    });
  }

  private void validLabelFunctionRule(Long taskId, LabelFunctionRule labelFunctionRule) {
    if (StringUtils.isAnyBlank(labelFunctionRule.getFunctionName(), labelFunctionRule.getFunctionBody())) {
      throw new BusinessIllegalStateException("标签函数规则格式错误", -1);
    }
    LabelFunctionTestReq req = new LabelFunctionTestReq();
    req.setExample("test");
    req.setFunctionName(labelFunctionRule.getFunctionName());
    req.setFunctionBody(labelFunctionRule.getFunctionBody());
    ResultVo<String> resultVo = labelFunctionTest(req);
    if (!"success".equals(resultVo.getMsg())) {
      throw new BusinessArgumentException("代码格式错误：" + resultVo.getData(), -1);
    }
    List<RuleInfoPO> list = ruleInfoService.getRuleListByTaskIdAndType(taskId,
        RuleType.LABEL_FUNCTION.getType());
    for (RuleInfoPO ruleInfoPO : list) {
      try {
        LabelFunctionRule other = objectMapper.readValue(ruleInfoPO.getMetadata(), LabelFunctionRule.class);
        if (labelFunctionRule.getFunctionName().equals(other.getFunctionName())) {
          throw new BusinessIllegalStateException("函数名称重复，请修改名称", -1);
        }
      } catch (JsonProcessingException e) {
        throw new BusinessIllegalStateException("规则：" + ruleInfoPO.getRuleName() + "格式错误！", -1);
      }
    }
    labelFunctionRule.setFunctionBody(labelFunctionRule.getFunctionBody());
  }

  private void validDataBaseRule(DatabaseRule databaseRule) {
    if (StringUtils.isAnyBlank(databaseRule.getDatabase(), databaseRule.getTable(),
        databaseRule.getLabelColumn(), databaseRule.getSentenceColumn(),
        databaseRule.getHost(), databaseRule.getUser(), databaseRule.getPassword()) || databaseRule.getPort() == null) {
      throw new BusinessIllegalStateException("数据库规则填写不完整！", -1);
    }
  }

  private void validOpenApiRule(OpenApiRule openApiRule) {
    if (StringUtils.isBlank(openApiRule.getHost())) {
      throw new BusinessIllegalStateException("外部系统接口地址不能为空！", -1);
    }
  }


  private void removeRuleResult(RuleInfoPO ruleInfoPO) {
    // 删除规则下测试集数据的运行结果
    ruleResultService.deleteByTaskIdAndRuleId(ruleInfoPO.getTaskId(), ruleInfoPO.getId());
    // 删除规则下未标注数据的运行结果
    ruleUnlabeledResultService.deleteByTaskIdAndRuleId(ruleInfoPO.getTaskId(), ruleInfoPO.getId());
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }

  @Autowired
  public void setDatasetInfoService(DatasetInfoService datasetInfoService) {
    this.datasetInfoService = datasetInfoService;
  }

  @Autowired
  public void setTaskExpertService(TaskExpertService taskExpertService) {
    this.taskExpertService = taskExpertService;
  }

  @Autowired
  public void setRuleOverviewService(RuleOverviewService ruleOverviewService) {
    this.ruleOverviewService = ruleOverviewService;
  }

  @Autowired
  public void setPublisher(RuleTaskAppendEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Autowired
  public void setRuleResultService(RuleResultService ruleResultService) {
    this.ruleResultService = ruleResultService;
  }

  @Autowired
  public void setRuleUnlabeledResultService(RuleUnlabeledResultService ruleUnlabeledResultService) {
    this.ruleUnlabeledResultService = ruleUnlabeledResultService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setRestTemplate(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setAlgorithmProperties(AlgorithmProperties algorithmProperties) {
    this.algorithmProperties = algorithmProperties;
  }

  @Autowired
  public void setUnlabelDataService(UnlabelDataService unlabelDataService) {
    this.unlabelDataService = unlabelDataService;
  }

  @Autowired
  public void setCommonProperties(CommonProperties commonProperties) {
    this.commonProperties = commonProperties;
  }
}
