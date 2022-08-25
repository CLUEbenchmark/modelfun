package com.wl.xc.modelfun.controller;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.RULE_DELETE;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.validation.group.Base;
import com.wl.xc.modelfun.commons.validation.group.Create;
import com.wl.xc.modelfun.commons.validation.group.Delete;
import com.wl.xc.modelfun.commons.validation.group.Retrieve;
import com.wl.xc.modelfun.commons.validation.group.Update;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule;
import com.wl.xc.modelfun.entities.model.LoginUserInfo;
import com.wl.xc.modelfun.entities.model.OpenApiRule;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.req.LabelFunctionTestReq;
import com.wl.xc.modelfun.entities.req.LabelPageReq;
import com.wl.xc.modelfun.entities.req.RegexTestReq;
import com.wl.xc.modelfun.entities.req.RuleDataReq;
import com.wl.xc.modelfun.entities.req.RuleOpReq;
import com.wl.xc.modelfun.entities.req.RuleResultReq;
import com.wl.xc.modelfun.entities.req.TaskIdPageReq;
import com.wl.xc.modelfun.entities.req.TaskIdReq;
import com.wl.xc.modelfun.entities.vo.DictKeyValueVO;
import com.wl.xc.modelfun.entities.vo.ExpertVO;
import com.wl.xc.modelfun.entities.vo.LabelHFWordVO;
import com.wl.xc.modelfun.entities.vo.LabelRuleVO;
import com.wl.xc.modelfun.entities.vo.PageResultVo;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ParseTaskVO;
import com.wl.xc.modelfun.entities.vo.RegexMatchDataVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.RuleMistakeVO;
import com.wl.xc.modelfun.entities.vo.RuleOverviewVO;
import com.wl.xc.modelfun.service.LabelRuleService;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.tasks.rule.RuleTaskAppendEventPublisher;
import com.wl.xc.modelfun.utils.ServletUserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 1.0
 * @date 2022/4/12 9:38
 */
@Tag(name = "标注规则管理")
@Slf4j
@RestController
@RequestMapping("/rule")
public class LabelRuleController {

  private LabelRuleService labelRuleService;

  private RuleInfoService ruleInfoService;

  private RuleTaskAppendEventPublisher publisher;

  @Operation(method = "POST", summary = "根据任务id获取规则列表")
  @GetMapping("/list/{taskId}")
  public ResultVo<List<LabelRuleVO>> getRuleList(@PathVariable("taskId") Long taskId) {
    return labelRuleService.getRuleList(taskId);
  }

  @Operation(method = "POST", summary = "根据任务id获取标签列表")
  @PostMapping("/label/page")
  public PageResultVo<List<DictKeyValueVO>> getLabelPageByTaskId(@Validated @RequestBody LabelPageReq req) {
    PageVO<DictKeyValueVO> result = labelRuleService.getLabelPageByTaskId(req);
    return PageResultVo.createSuccess(result);
  }

  @Operation(method = "POST", summary = "添加标注规则")
  @PostMapping("/add")
  public ResultVo<Boolean> addRule(@Validated({Create.class}) @RequestBody RuleOpReq req) {
    return labelRuleService.addRule(req);
  }

  /**
   * 根据规则id删除规则
   *
   * @param req 规则删除请求
   * @return 删除结果
   */
  @Operation(method = "POST", summary = "根据规则id删除规则")
  @PostMapping("/delete")
  public ResultVo<Boolean> deleteRule(@Validated({Delete.class}) @RequestBody RuleOpReq req) {
    ResultVo<Boolean> resultVo = labelRuleService.deleteRule(req);
    if (resultVo.getSuccess()) {
      // 重新计算规则概览准确率和覆盖率,这里把消息发送放在外层，主要是为了让之前的事务提交完成后再发送消息
      RuleInfoPO ruleInfoPO = ruleInfoService.getById(req.getRuleId());
      LoginUserInfo userInfo = ServletUserHolder.getUserByContext();
      HashMap<String, Object> map = new HashMap<>();
      map.put(SESSION_UID, userInfo.getUid());
      map.put(RULE_DELETE, true);
      publisher.publish(req.getTaskId(), ruleInfoPO, RuleTaskType.GLOBAL, map);
    }
    return resultVo;
  }

  @Operation(method = "POST", summary = "更新规则内容")
  @PostMapping("/update")
  public ResultVo<Boolean> updateRule(@Validated({Update.class}) @RequestBody RuleOpReq req) {
    return labelRuleService.updateRule(req);
  }

  @Operation(method = "GET", summary = "根据任务id获取专家知识文件列表")
  @GetMapping("/expert/{taskId}/list")
  public ResultVo<List<ExpertVO>> getTaskExpertList(@PathVariable("taskId") Long taskId) {
    return labelRuleService.getTaskExpertList(taskId);
  }

  /**
   * 根据任务ID获取规则概览
   *
   * @param taskId 任务ID
   * @return 规则概览
   */
  @Operation(method = "GET", summary = "根据任务id获取规则概览")
  @GetMapping("/overview/{taskId}")
  public ResultVo<RuleOverviewVO> getRuleOverview(@PathVariable("taskId") Long taskId) {
    return labelRuleService.getRuleOverview(taskId);
  }

  @Operation(method = "POST", summary = "label function功能的测试")
  @PostMapping("/lf/test")
  public ResultVo<String> labelFunctionTest(@Validated @RequestBody LabelFunctionTestReq req) {
    return labelRuleService.labelFunctionTest(req);
  }

  @Operation(method = "GET", summary = "根据任务ID获取正在运行的规则")
  @GetMapping("/running/{taskId}")
  public ResultVo<Boolean> getRunningRuleByTaskId(@PathVariable("taskId") Long taskId) {
    return labelRuleService.getRunningRuleByTaskId(taskId);
  }

  @Operation(method = "POST", summary = "获取规则下标签和测试集不一致的数据")
  @PostMapping("/lf/mistake_list")
  public PageResultVo<List<RuleMistakeVO>> getMistakeList(@Validated @RequestBody RuleResultReq req) {
    PageVO<RuleMistakeVO> result = labelRuleService.getMistakeList(req);
    return PageResultVo.createSuccess(result);
  }

  @Operation(method = "POST", summary = "获取规则未覆盖的测试集数据")
  @PostMapping("/unCoverage_list")
  public PageResultVo<List<RuleMistakeVO>> getUnCoverageList(
      @Validated({Retrieve.class, Base.class}) @RequestBody RuleDataReq req) {
    PageVO<RuleMistakeVO> result = labelRuleService.getUnCoverageList(req);
    return PageResultVo.createSuccess(result);
  }

  @Operation(method = "POST", summary = "实时计算模式匹配规则的未标注数据匹配结果")
  @PostMapping("/data_match_list")
  public ResultVo<RegexMatchDataVO> getRegexMatchData(@Validated(Base.class) @RequestBody RuleDataReq req) {
    if (StringUtils.isBlank(req.getMetadata())) {
      RegexMatchDataVO vo = new RegexMatchDataVO();
      vo.setDataList(Collections.emptyList());
      vo.setCoverage("0%");
      return ResultVo.createSuccess(vo);
    }
    return labelRuleService.getRegexMatchData(req);
  }

  @Operation(method = "POST", summary = "获取任务下标签对应的高频词汇统计")
  @PostMapping("/label_hf_word")
  public PageResultVo<List<LabelHFWordVO>> getLabelHfWord(@Validated @RequestBody TaskIdPageReq req) {
    PageVO<LabelHFWordVO> result = labelRuleService.getLabelHfWord(req);
    return PageResultVo.createSuccess(result);
  }

  @Operation(method = "POST", summary = "查询是否有专家知识文件解析任务正在运行")
  @PostMapping("/is_exist_expert_task")
  public ResultVo<ParseTaskVO> isExistExpertTask(@Validated @RequestBody TaskIdReq req) {
    return labelRuleService.isExistExpertTask(req);
  }

  @Operation(method = "POST", summary = "GPT接口测试")
  @PostMapping("/gpt_test")
  public ResultVo<String> gptLfTest(@Validated @RequestBody BuiltinModelRule req) {
    return labelRuleService.gptLfTest(req);
  }

  @Operation(method = "POST", summary = "模式匹配接口测试")
  @PostMapping("/regex_test")
  public ResultVo<String> regexTest(@Validated @RequestBody RegexTestReq req) {
    return labelRuleService.regexTest(req);
  }

  @Operation(method = "POST", summary = "外部接口测试")
  @PostMapping("/openapi_test")
  public ResultVo<String> openApiTest(@RequestBody OpenApiRule req) {
    if (StringUtils.isBlank(req.getHost())) {
      return ResultVo.create("接口地址不能为空！", -1, false, null);
    }
    if (StringUtils.isBlank(req.getRequestBody())) {
      return ResultVo.create("测试文本不能为空！", -1, false, null);
    }
    if (req.getTaskId() == null) {
      return ResultVo.create("任务ID不能为空！", -1, false, null);
    }
    return labelRuleService.openApiTest(req);
  }

  @Autowired
  public void setLabelRuleService(LabelRuleService labelRuleService) {
    this.labelRuleService = labelRuleService;
  }

  @Autowired
  public void setPublisher(RuleTaskAppendEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }
}
