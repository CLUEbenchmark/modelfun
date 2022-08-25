package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.RuleResultPO;

/**
 * @version 1.0
 * @date 2022/4/15 12:18
 */
public interface RuleResultForBatchMapper extends BaseMapper<RuleResultPO> {

  int insertSelective(RuleResultPO ruleResultPO);
}