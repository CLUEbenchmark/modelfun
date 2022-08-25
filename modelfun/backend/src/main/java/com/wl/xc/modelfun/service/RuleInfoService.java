package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.mapper.RuleInfoMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class RuleInfoService extends ServiceImpl<RuleInfoMapper, RuleInfoPO> {

  private GptCacheService gptCacheService;

  public List<RuleInfoPO> getRuleListByTaskId(Long taskId) {
    return baseMapper.selectList(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_TASK_ID, taskId)
            .gt(RuleInfoPO.COL_RULE_TYPE, 0));
  }

  public List<RuleInfoPO> getRuleListByTaskIdAndType(Long taskId, Integer ruleType) {
    return baseMapper.selectList(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_TASK_ID, taskId)
            .eq(RuleInfoPO.COL_RULE_TYPE, ruleType));
  }

  public boolean deleteRuleById(Long ruleId) {
    return baseMapper.delete(Wrappers.<RuleInfoPO>query().eq(RuleInfoPO.COL_ID, ruleId)) > 0;
  }

  @Transactional(rollbackFor = Exception.class)
  public void deleteRuleAndCache(Long taskId, Long ruleId) {
    gptCacheService.deleteCache(taskId);
    baseMapper.delete(Wrappers.<RuleInfoPO>query().eq(RuleInfoPO.COL_ID, ruleId));
  }

  public int deleteByTaskIdAndRuleType(Long taskId, Integer ruleType) {
    return baseMapper.delete(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_TASK_ID, taskId)
            .eq(RuleInfoPO.COL_RULE_TYPE, ruleType));
  }

  public int updateByIdSelective(RuleInfoPO entity) {
    return baseMapper.updateByIdSelective(entity);
  }

  public List<RuleInfoPO> selectBySelective(RuleInfoPO entity) {
    List<RuleInfoPO> poList = baseMapper.selectBySelective(entity);
    poList.removeIf(p -> p.getRuleType() < 0);
    return poList;
  }

  public Long countRuleByTaskId(Long taskId) {
    return baseMapper.selectCount(Wrappers.<RuleInfoPO>query().eq(RuleInfoPO.COL_TASK_ID, taskId));
  }

  public Long countRuleComplete(Long taskId) {
    return baseMapper.selectCount(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_TASK_ID, taskId)
            .eq(RuleInfoPO.COL_COMPLETED, 1)
            .gt(RuleInfoPO.COL_RULE_TYPE, 0));
  }

  public Long countRunningRule(Long taskId) {
    return baseMapper.selectCount(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_TASK_ID, taskId)
            .eq(RuleInfoPO.COL_COMPLETED, 0)
            .gt(RuleInfoPO.COL_RULE_TYPE, 0));
  }

  public Long countRunningRuleByType(Integer ruleType) {
    return baseMapper.selectCount(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_COMPLETED, 0)
            .eq(RuleInfoPO.COL_RULE_TYPE, ruleType));
  }

  public int deleteSelective(RuleInfoPO po) {
    return baseMapper.deleteSelective(po);
  }

  @Autowired
  public void setGptCacheService(GptCacheService gptCacheService) {
    this.gptCacheService = gptCacheService;
  }

  public RuleInfoPO getLastUpdateRule(Long taskId) {
    return baseMapper.selectOne(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_TASK_ID, taskId)
            .orderByDesc(RuleInfoPO.COL_UPDATE_DATETIME)
            .last("limit 1"));
  }

  public RuleInfoPO getRuleByType(Long taskId, Integer ruleType) {
    return baseMapper.selectOne(
        Wrappers.<RuleInfoPO>query()
            .eq(RuleInfoPO.COL_TASK_ID, taskId)
            .eq(RuleInfoPO.COL_RULE_TYPE, ruleType)
            .last("limit 1"));
  }

  public List<RuleInfoPO> getRuleTemplateByTaskId(Long taskId) {
    return baseMapper.getRuleTemplateByTaskId(taskId);
  }
}
