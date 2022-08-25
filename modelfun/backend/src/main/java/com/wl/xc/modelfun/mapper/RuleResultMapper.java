package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.po.RuleResultPO;
import com.wl.xc.modelfun.entities.po.RuleVotePO;
import com.wl.xc.modelfun.entities.vo.RuleMistakeVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/15 12:18
 */
public interface RuleResultMapper extends BaseMapper<RuleResultPO> {

  List<RuleVotePO> selectRuleVote(@Param("taskId") Long taskId);

  List<RuleVotePO> selectTestDataVoteGroup(@Param("taskId") Long taskId, @Param("offset") Long offset,
      @Param("size") Long size, @Param("dataType") Integer dataType);

  IPage<RuleMistakeVO> getMistakeByTaskIdAndRule(IPage<RuleMistakeVO> page, @Param("po") RuleResultPO ruleResultPO);

  IPage<RuleMistakeVO> getUnCoverageByTaskId(IPage<RuleMistakeVO> page, @Param("po") RuleResultPO ruleResultPO);

  Integer countLabeledSentence(@Param("taskId") Long taskId);

  IPage<RuleMistakeVO> getUnCoverageByTaskIdAndRule(IPage<RuleMistakeVO> page, @Param("po") RuleResultPO ruleResultPO);

  IPage<RuleMistakeVO> getUnCoverageForRegex(IPage<RuleMistakeVO> page, @Param("po") RuleResultPO ruleResultPO);

  int copyRuleResultFromTemplate(@Param("srcTask") Long srcTask, @Param("destTask") Long destTask,
      @Param("srcRule") Long srcRule, @Param("destRule") Long destRule);
}