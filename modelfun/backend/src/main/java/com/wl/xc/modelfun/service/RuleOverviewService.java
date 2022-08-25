package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.RuleOverviewPO;
import com.wl.xc.modelfun.mapper.RuleOverviewMapper;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/15 12:18
 */
@Service
public class RuleOverviewService extends ServiceImpl<RuleOverviewMapper, RuleOverviewPO> {

  public RuleOverviewPO getRuleOverviewByTaskId(Long taskId) {
    return baseMapper.getOneByTaskId(taskId);
  }

  /**
   * upsert操作，当数据库中存在该taskId的记录时，更新，否则插入
   *
   * @param ruleOverviewPO 规则概览
   */
  public void insertRuleOverview(RuleOverviewPO ruleOverviewPO) {
    baseMapper.insertOrUpdateSelective(ruleOverviewPO);
  }

  public RuleOverviewPO getTemplateByTaskId(Long taskId) {
    return baseMapper.getTemplateByTaskId(taskId);
  }

}
