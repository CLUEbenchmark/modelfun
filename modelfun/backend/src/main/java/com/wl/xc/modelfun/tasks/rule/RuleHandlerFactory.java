package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 获取规则处理器的工厂
 *
 * @version 1.0
 * @date 2022.4.16 10:23
 */
@Component
public class RuleHandlerFactory {

  private final Map<RuleType, RuleHandlerConstructor> CON_MAP = new HashMap<>();

  public RuleHandlerFactory() {
  }

  public RuleTaskHandler getRuleHandler(RuleInfoPO ruleInfoPO) {
    RuleDescribe ruleDescribe = new RuleDescribe();
    RuleType ruleType = RuleType.getByType(ruleInfoPO.getRuleType());
    // 这里不用进行规则类型的检查，因为能入库的规则都是经过校验的
    ruleDescribe.setRuleType(ruleType);
    ruleDescribe.setMetadata(ruleInfoPO.getMetadata());
    ruleDescribe.setTaskId(ruleInfoPO.getTaskId());
    ruleDescribe.setLabelId(ruleInfoPO.getLabel());
    ruleDescribe.setRuleId(ruleInfoPO.getId());
    RuleHandlerConstructor constructor = CON_MAP.get(ruleType);
    return constructor.createHandler(ruleDescribe);
  }

  @Autowired(required = false)
  public void addRuleHandlerConstructor(List<RuleHandlerConstructor> ruleHandlerConstructors) {
    if (ruleHandlerConstructors != null) {
      for (RuleHandlerConstructor ruleHandlerConstructor : ruleHandlerConstructors) {
        CON_MAP.put(ruleHandlerConstructor.getRuleType(), ruleHandlerConstructor);
      }
    }
  }
}
