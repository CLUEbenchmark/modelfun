package com.wl.xc.modelfun.tasks.rule;

import java.time.Clock;
import org.springframework.context.ApplicationEvent;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:44
 */
public class RuleTaskAppendEvent extends ApplicationEvent {

  private final RuleTask ruleTask;

  public RuleTaskAppendEvent(RuleTask ruleTask) {
    super(ruleTask);
    this.ruleTask = ruleTask;
  }

  public RuleTaskAppendEvent(RuleTask ruleTask, Clock clock) {
    super(ruleTask, clock);
    this.ruleTask = ruleTask;
  }

  public RuleTask getRuleTask() {
    return ruleTask;
  }

}
