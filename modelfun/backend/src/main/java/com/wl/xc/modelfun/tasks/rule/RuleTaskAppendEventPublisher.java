package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import java.util.Map;
import javax.annotation.Nonnull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022.4.16 16:00
 */
@Component
public class RuleTaskAppendEventPublisher implements ApplicationEventPublisherAware {

  private ApplicationEventPublisher publisher;

  public void publish(Long taskId, RuleInfoPO ruleInfo, RuleTaskType ruleTaskType, Map<String, Object> params) {
    RuleTask ruleTask = new RuleTask();
    ruleTask.setTaskId(taskId);
    ruleTask.setRuleInfo(ruleInfo);
    ruleTask.setType(ruleTaskType);
    if (params != null) {
      ruleTask.getConfig().setParams(params);
    }
    publish(ruleTask);
  }

  public void publish(RuleTask ruleTask) {
    publisher.publishEvent(new RuleTaskAppendEvent(ruleTask));
  }

  public void publish(RuleTaskAppendEvent event) {
    publisher.publishEvent(event);
  }

  @Override
  public void setApplicationEventPublisher(@Nonnull ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }
}
