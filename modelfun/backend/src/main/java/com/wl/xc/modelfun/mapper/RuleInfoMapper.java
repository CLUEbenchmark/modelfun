package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface RuleInfoMapper extends BaseMapper<RuleInfoPO> {

  int updateByIdSelective(RuleInfoPO ruleInfoPO);

  List<RuleInfoPO> selectBySelective(RuleInfoPO ruleInfoPO);

  int deleteSelective(RuleInfoPO ruleInfoPO);

  List<RuleInfoPO> getRuleTemplateByTaskId(Long taskId);
}