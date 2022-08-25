package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.RuleType;

/**
 * 规则的构造类
 *
 * @version 1.0
 * @date 2022.4.16 10:59
 */
public interface RuleHandlerConstructor {

  RuleType getRuleType();

  RuleTaskHandler createHandler(RuleDescribe ruleDescribe);
}
