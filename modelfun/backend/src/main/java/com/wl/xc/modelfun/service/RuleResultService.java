package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.RuleResultPO;
import com.wl.xc.modelfun.entities.po.RuleVotePO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.entities.vo.RuleMistakeVO;
import com.wl.xc.modelfun.mapper.RuleResultMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/15 12:18
 */
@Service
public class RuleResultService extends ServiceImpl<RuleResultMapper, RuleResultPO> {

  /**
   * 从测试集规则运行结果中查询出投票结果
   *
   * @param taskId 任务id
   * @return 投票结果
   */
  public List<RuleVotePO> getVoteByTaskId(Long taskId) {
    return baseMapper.selectRuleVote(taskId);
  }

  public List<RuleVotePO> selectTestDataVoteGroup(Long taskId, Long offset, Long size, boolean show) {
    return baseMapper.selectTestDataVoteGroup(taskId, offset, size, show ? 4 : 5);
  }

  public Long countByTaskId(Long taskId) {
    return baseMapper.selectCount(Wrappers.<RuleResultPO>query().eq(RuleResultPO.COL_TASK_ID, taskId));
  }

  public int deleteByTaskIdAndRuleId(Long taskId, Long ruleId) {
    return baseMapper.delete(
        Wrappers.<RuleResultPO>query()
            .eq(RuleResultPO.COL_TASK_ID, taskId)
            .eq(RuleResultPO.COL_RULE_ID, ruleId));
  }

  public int deleteAllByTaskId(Long taskId) {
    return baseMapper.delete(
        Wrappers.<RuleResultPO>query()
            .eq(RuleResultPO.COL_TASK_ID, taskId));
  }

  public void saveForBatchNoLog(List<RuleResultPO> list) {
    if (list == null || list.size() == 0) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.RuleResultForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public PageVO<RuleMistakeVO> getMistakeByTaskIdAndRule(RuleResultPO ruleResultPO, IPage<RuleMistakeVO> page) {
    baseMapper.getMistakeByTaskIdAndRule(page, ruleResultPO);
    return PageUtil.convert(page);
  }

  /**
   * 获取任务下的未覆盖的所有验证集数据。对于模式匹配的规则，只获取模式匹配设置的label的数据
   *
   * @param ruleResultPO 查询条件
   * @param page         分页参数
   * @return 分页结果
   */
  public PageVO<RuleMistakeVO> getUnCoverageByTaskIdAndRule(RuleResultPO ruleResultPO, IPage<RuleMistakeVO> page) {
    if (ruleResultPO.getLabelId() != null) {
      baseMapper.getUnCoverageForRegex(page, ruleResultPO);
    } else {
      baseMapper.getUnCoverageByTaskIdAndRule(page, ruleResultPO);
    }
    return PageUtil.convert(page);
  }

  public PageVO<RuleMistakeVO> getUnCoverageByTaskId(RuleResultPO ruleResultPO, IPage<RuleMistakeVO> page) {
    baseMapper.getUnCoverageByTaskId(page, ruleResultPO);
    return PageUtil.convert(page);
  }

  /**
   * 获取任务下所有展示的测试集数据，其被打上标签的总数（根据语料ID去重）
   *
   * @param taskId 任务id
   * @return 语料总数
   */
  public Integer countLabeledSentence(Long taskId) {
    return baseMapper.countLabeledSentence(taskId);
  }

  public int copyRuleResultFromTemplate(Long srcTask, Long destTask, Long srcRule, Long destRule) {
    return baseMapper.copyRuleResultFromTemplate(srcTask, destTask, srcRule, destRule);
  }
}
