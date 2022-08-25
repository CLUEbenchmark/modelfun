package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.RuleOverviewPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/15 12:18
 */
public interface RuleOverviewMapper extends BaseMapper<RuleOverviewPO> {

  RuleOverviewPO getOneByTaskId(@Param("taskId") Long taskId);

  int insertOrUpdateSelective(RuleOverviewPO ruleOverviewPO);

  RuleOverviewPO getTemplateByTaskId(Long taskId);
}