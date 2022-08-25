package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.RuleUnlabeledResultPO;

/**
 * @version 1.0
 * @date 2022.4.15 23:40
 */
public interface RuleUnlabeledResultForBatchMapper extends BaseMapper<RuleUnlabeledResultPO> {

  int insertSelective(RuleUnlabeledResultPO ruleUnlabeledResultPO);
}