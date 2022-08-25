package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.wl.xc.modelfun.entities.po.RuleUnlabeledResultPO;
import com.wl.xc.modelfun.entities.po.RuleVotePO;
import com.wl.xc.modelfun.mapper.RuleUnlabeledResultMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022.4.15 23:40
 */
@Service
public class RuleUnlabeledResultService
    extends ServiceImpl<RuleUnlabeledResultMapper, RuleUnlabeledResultPO> {

  public List<RuleUnlabeledResultPO> getAllByTaskIdAndRuleId(Long taskId, Long ruleId) {
    return baseMapper.selectList(
        Wrappers.<RuleUnlabeledResultPO>query()
            .eq(RuleUnlabeledResultPO.COL_TASK_ID, taskId)
            .eq(RuleUnlabeledResultPO.COL_RULE_ID, ruleId));
  }

  public int deleteByTaskIdAndRuleId(Long taskId, Long ruleId) {
    return baseMapper.delete(
        Wrappers.<RuleUnlabeledResultPO>query()
            .eq(RuleUnlabeledResultPO.COL_TASK_ID, taskId)
            .eq(RuleUnlabeledResultPO.COL_RULE_ID, ruleId));
  }

  public int deleteAllByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<RuleUnlabeledResultPO>query().eq(RuleUnlabeledResultPO.COL_TASK_ID, taskId));
  }

  public List<RuleVotePO> groupUnlabelResult(Long taskId) {
    return baseMapper.selectRuleVoteDistinct(taskId);
  }

  public Long countConflictResult(Long taskId) {
    return baseMapper.countConflictResult(taskId);
  }

  public List<RuleVotePO> selectUnlabeledDataVoteGroup(Long taskId, Long offset, Long size) {
    return baseMapper.selectUnlabeledDataVoteGroup(taskId, offset, size);
  }

  /**
   * 查询任务下未标注数据的覆盖数量，即只要有任务对一个语料进行标注，就表示该语料被覆盖
   *
   * @param taskId 任务id
   * @return 覆盖数量
   */
  public Long countCoverageResult(Long taskId) {
    return baseMapper.countCoverageResult(taskId);
  }

  public Long countByTaskIdAndRuleId(Long taskId, Long ruleId) {
    return baseMapper.selectCount(
        Wrappers.<RuleUnlabeledResultPO>query()
            .eq(RuleUnlabeledResultPO.COL_TASK_ID, taskId)
            .eq(RuleUnlabeledResultPO.COL_RULE_ID, ruleId));
  }

  public List<RuleUnlabeledResultPO> simplePageByTaskIdAndRuleId(Long taskId, Long ruleId, long offset, long size) {
    return baseMapper.simplePageByTaskIdAndRuleId(taskId, ruleId, offset, size);
  }

  public void saveForBatchNoLog(List<RuleUnlabeledResultPO> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    String statement = "com.wl.xc.modelfun.mapper.RuleUnlabeledResultForBatchMapper.insertSelective";
    SqlHelper.executeBatch(
        this.entityClass,
        super.log,
        list,
        list.size(),
        (sqlSession, entity) -> sqlSession.insert(statement, entity));
  }

  public void delByTaskIdAndSentenceId(Long taskId, Long sentenceId) {
    baseMapper.delete(
        Wrappers.<RuleUnlabeledResultPO>query()
            .eq(RuleUnlabeledResultPO.COL_TASK_ID, taskId)
            .eq(RuleUnlabeledResultPO.COL_SENTENCE_ID, sentenceId));
  }

  public int copyRuleUnlabeledResultFromTemplate(Long srcTask, Long destTask, Long srcRule, Long destRule) {
    return baseMapper.copyRuleUnlabeledResultFromTemplate(srcTask, destTask, srcRule, destRule);
  }
}
