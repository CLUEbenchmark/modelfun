package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.entities.dto.GPTCallbackDTO;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule;
import com.wl.xc.modelfun.entities.model.OpenApiRule;
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
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.ParseTaskVO;
import com.wl.xc.modelfun.entities.vo.RegexMatchDataVO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.entities.vo.RuleMistakeVO;
import com.wl.xc.modelfun.entities.vo.RuleOverviewVO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/4/12 9:54
 */
public interface LabelRuleService {

  /**
   * 根据任务ID查询标签规则
   *
   * @param taskId 任务ID
   * @return 标签规则
   */
  ResultVo<List<LabelRuleVO>> getRuleList(Long taskId);

  /**
   * 根据任务ID分页查询标签集内容
   *
   * @param req 分页请求
   * @return 标签集内容
   */
  PageVO<DictKeyValueVO> getLabelPageByTaskId(LabelPageReq req);

  /**
   * 创建标签规则
   *
   * @param req 标签规则请求
   * @return 标签规则
   */
  ResultVo<Boolean> addRule(RuleOpReq req);

  /**
   * 删除标签规则
   *
   * @param req 标签规则请求
   * @return 删除结果
   */
  ResultVo<Boolean> deleteRule(RuleOpReq req);

  /**
   * 更新规则内容
   *
   * @param req 标签规则请求
   * @return 更新结果
   */
  ResultVo<Boolean> updateRule(RuleOpReq req);

  /**
   * 根据任务ID查询专家知识列表
   *
   * @param taskId 任务ID
   * @return 专家知识列表
   */
  ResultVo<List<ExpertVO>> getTaskExpertList(Long taskId);

  /**
   * 根据任务ID查询标签规则概览
   *
   * @param taskId 任务ID请求
   * @return 标签规则概览
   */
  ResultVo<RuleOverviewVO> getRuleOverview(Long taskId);

  /**
   * 测试lf规则
   *
   * @param req 测试请求
   * @return 测试结果
   */
  ResultVo<String> labelFunctionTest(LabelFunctionTestReq req);

  /**
   * 根据任务ID获取正在运行的规则
   *
   * @param taskId 任务ID
   * @return 是否有规则正在运行
   */
  ResultVo<Boolean> getRunningRuleByTaskId(Long taskId);

  /**
   * 获取标注规则下，标注结果和测试集不一致的语料
   *
   * @param req 请求
   * @return 错误语料
   */
  PageVO<RuleMistakeVO> getMistakeList(RuleResultReq req);

  /**
   * 获取规则下，未覆盖到的测试集语料.
   * <p>
   * 如果是模式匹配的规则下，则获取的是对应标签的语料，如果是其他类型的规则，获取的是所有未覆盖的语料。
   *
   * @param req 请求
   * @return 未覆盖语料
   */
  PageVO<RuleMistakeVO> getUnCoverageList(RuleDataReq req);

  /**
   * 获取标签对应的高频词
   *
   * @param req 请求
   * @return 高频词
   */
  PageVO<LabelHFWordVO> getLabelHfWord(TaskIdPageReq req);

  /**
   * 查询是否存在专家知识解析任务，如果存在，则返回任务ID
   *
   * @param req 请求
   * @return 查询结果
   */
  ResultVo<ParseTaskVO> isExistExpertTask(TaskIdReq req);

  /**
   * gpt回调接口
   *
   * @param callbackDTO 回调对象
   * @return 回调结果
   */
  ResultVo<Boolean> saveGPTResultAsync(GPTCallbackDTO callbackDTO);

  /**
   * gpt接口测试，该接口会很耗时
   *
   * @param req 测试请求
   * @return 测试结果
   */
  ResultVo<String> gptLfTest(BuiltinModelRule req);

  /**
   * 模式匹配测试
   *
   * @param req 请求
   * @return 结果
   */
  ResultVo<String> regexTest(RegexTestReq req);

  /**
   * 外部api测试
   *
   * @param req 请求
   * @return 结果
   */
  ResultVo<String> openApiTest(OpenApiRule req);

  /**
   * 实时计算模式匹配下匹配的未标注集合
   *
   * @param req 查询数据
   * @return 查询结果
   */
  ResultVo<RegexMatchDataVO> getRegexMatchData(RuleDataReq req);
}
