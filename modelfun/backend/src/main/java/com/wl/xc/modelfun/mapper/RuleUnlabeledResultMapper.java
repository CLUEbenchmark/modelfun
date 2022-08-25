package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.RuleUnlabeledResultPO;
import com.wl.xc.modelfun.entities.po.RuleVotePO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022.4.15 23:40
 */
public interface RuleUnlabeledResultMapper extends BaseMapper<RuleUnlabeledResultPO> {

  List<RuleVotePO> selectRuleVoteDistinct(@Param("taskId") Long taskId);

  Long countConflictResult(@Param("taskId") Long taskId);

  Long countCoverageResult(@Param("taskId") Long taskId);

  List<RuleUnlabeledResultPO> simplePageByTaskIdAndRuleId(@Param("taskId") Long taskId, @Param("ruleId") Long ruleId,
      @Param("offset") Long offset, @Param("size") Long size);

  List<RuleVotePO> selectUnlabeledDataVoteGroup(@Param("taskId") Long taskId, @Param("offset") Long offset,
      @Param("size") Long size);

  int copyRuleUnlabeledResultFromTemplate(@Param("srcTask") Long srcTask, @Param("destTask") Long destTask,
      @Param("srcRule") Long srcRule, @Param("destRule") Long destRule);
}