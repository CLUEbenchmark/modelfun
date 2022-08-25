package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.RuleTaskType;

/**
 * @version 1.0
 * @date 2022.4.16 15:16
 */
public interface RuleTaskExecute {

  RuleTaskType getRuleTaskType();

  void execute(RuleTask ruleTask);
}
