package com.wl.xc.modelfun.tasks.rule;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.service.RuleInfoService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/4/22 10:17
 */
@Slf4j
@Component
public class BatchRuleTaskExecute implements RuleTaskExecute {

  private RuleHandleService ruleHandleService;

  private RuleInfoService ruleInfoService;

  private RuleTaskAppendEventPublisher publisher;

  @Override
  public RuleTaskType getRuleTaskType() {
    return RuleTaskType.BATCH;
  }

  @Override
  public void execute(RuleTask ruleTask) {
    List<RuleInfoPO> poList = ruleTask.getRuleInfoPOList();
    boolean isOneSuccess = false;
    for (RuleInfoPO ruleInfoPO : poList) {
      RuleTask task = new RuleTask();
      task.setTaskId(ruleTask.getTaskId());
      task.setRuleInfo(ruleInfoPO);
      task.setType(RuleTaskType.SINGLE);
      try {
        ruleHandleService.calculate(task);
        isOneSuccess = true;
      } catch (Exception e) {
        log.error(
            "[BatchRuleTaskExecute.execute] 规则任务运行失败，taskId={}, ruleName:{}",
            task.getTaskId(),
            task.getRuleInfo().getRuleName(),
            e);
        // 规则任务运行失败，通知规则任务失败事件，设置规则任务状态为失败
        RuleInfoPO po = new RuleInfoPO();
        po.setCompleted(2);
        po.setId(task.getRuleInfo().getId());
        po.setUpdateDatetime(LocalDateTime.now());
        ruleInfoService.updateByIdSelective(po);
      }
    }
    // 通知全局计算重叠率和冲突率
    if (isOneSuccess) {
      Map<String, Object> map = new HashMap<>();
      map.put(SESSION_UID, ruleTask.getConfig().get(SESSION_UID).orElse(null));
      publisher.publish(ruleTask.getTaskId(), null, RuleTaskType.GLOBAL, map);
    }
  }

  @Autowired
  public void setRuleHandleService(RuleHandleService ruleHandleService) {
    this.ruleHandleService = ruleHandleService;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setPublisher(RuleTaskAppendEventPublisher publisher) {
    this.publisher = publisher;
  }
}
