package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * 规则处理上下文
 *
 * @version 1.0
 * @date 2022/4/15 16:42
 */
@Slf4j
@Component
public class RuleHandleService {

  /**
   * 标注规则
   */
  private RuleHandlerFactory ruleHandlerFactory;

  private SingleRuleLabelService singleRuleLabelService;

  public void calculate(RuleTask ruleTask) {
    log.info("[RuleHandleService.calculate] 开始计算规则参数");
    RuleInfoPO ruleInfo = ruleTask.getRuleInfo();
    // 根据规则信息获取规则处理器
    RuleTaskHandler handler = getRuleTaskHandler(ruleInfo);
    if (handler == null) {
      throw new BusinessIllegalStateException("错误的规则类型！");
    }
    try {
      log.info("[RuleHandleContext.calculate] 开始进行规则参数计算！规则名称：{}", ruleInfo.getRuleName());
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();
      handler.init();
      stopWatch.stop();
      log.info("[RuleHandleService.calculate] 准备工作完成，耗时：{}秒", stopWatch.getLastTaskInfo().getTimeSeconds());
      stopWatch.start();
      singleRuleLabelService.label(ruleTask, handler);
      stopWatch.stop();
      log.info("[RuleHandleService.calculate] 规则打标完成，耗时：{}秒", stopWatch.getLastTaskInfo().getTimeSeconds());
      log.info("[RuleHandleContext.calculate] 规则参数计算结束！总计耗时：{}秒", stopWatch.getTotalTimeSeconds());
    } finally {
      handler.destroy();
    }
  }

  private RuleTaskHandler getRuleTaskHandler(RuleInfoPO ruleInfo) {
    return ruleHandlerFactory.getRuleHandler(ruleInfo);
  }

  @Autowired
  public void setRuleHandlerFactory(RuleHandlerFactory ruleHandlerFactory) {
    this.ruleHandlerFactory = ruleHandlerFactory;
  }

  @Autowired
  public void setSingleRuleLabelService(SingleRuleLabelService singleRuleLabelService) {
    this.singleRuleLabelService = singleRuleLabelService;
  }
}
