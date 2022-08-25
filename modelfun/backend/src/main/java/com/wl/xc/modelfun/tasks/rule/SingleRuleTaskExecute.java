package com.wl.xc.modelfun.tasks.rule;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022.4.16 15:23
 */
@Slf4j
@Component
public class SingleRuleTaskExecute implements RuleTaskExecute {

  private RuleHandleService ruleHandleService;

  private RuleInfoService ruleInfoService;

  private TaskInfoService taskInfoService;

  private RuleTaskAppendEventPublisher publisher;

  @Override
  public RuleTaskType getRuleTaskType() {
    return RuleTaskType.SINGLE;
  }

  @Override
  public void execute(RuleTask ruleTask) {
    TaskInfoPO po = taskInfoService.getById(ruleTask.getTaskId());
    try {
      ruleHandleService.calculate(ruleTask);
      Optional<String> uid = ruleTask.getConfig().getByType(SESSION_UID, String.class);
      uid.ifPresent(
          s -> {
            WebsocketDTO dto = new WebsocketDTO();
            String msg = String.format("标注规则运行成功，规则名称：%s", ruleTask.getRuleInfo().getRuleName());
            dto.setEvent(WsEventType.RULE_SUCCESS);
            dto.setData(WebsocketDataDTO.create(po.getId(), po.getName(), msg, true));
            WebSocketHandler.sendByUid(s, dto);
          });
      // 通知全局计算重叠率和冲突率
      publisher.publish(ruleTask.getTaskId(), null, RuleTaskType.GLOBAL, ruleTask.getConfig().getParams());
    } catch (Exception e) {
      log.error(
          "[SingleRuleTaskExecute.execute] 规则任务运行失败，taskId={}, ruleName:{}",
          ruleTask.getTaskId(),
          ruleTask.getRuleInfo().getRuleName(),
          e);
      // 规则任务运行失败，通知规则任务失败事件，设置规则任务状态为失败
      RuleInfoPO ruleInfoPO = new RuleInfoPO();
      ruleInfoPO.setCompleted(2);
      ruleInfoPO.setId(ruleTask.getRuleInfo().getId());
      ruleInfoPO.setUpdateDatetime(LocalDateTime.now());
      ruleInfoService.updateByIdSelective(ruleInfoPO);
      Optional<String> uid = ruleTask.getConfig().getByType(SESSION_UID, String.class);
      uid.ifPresent(s -> {
        WebsocketDTO dto = new WebsocketDTO();
        String msg;
        if (e instanceof BusinessException) {
          msg = String.format("标注规则运行失败，规则名称：%s，失败原因：%s", ruleTask.getRuleInfo().getRuleName(),
              e.getMessage());
        } else {
          msg = String.format("标注规则运行失败，规则名称：%s，失败原因：%s", ruleTask.getRuleInfo().getRuleName(),
              "服务器内部错误，请联系管理员");
        }
        dto.setEvent(WsEventType.RULE_FAIL);
        dto.setData(WebsocketDataDTO.create(po.getId(), po.getName(), msg, false));
        WebSocketHandler.sendByUid(s, dto);
      });
    }
  }

  @Autowired
  public void setRuleHandleService(RuleHandleService ruleHandleService) {
    this.ruleHandleService = ruleHandleService;
  }

  @Autowired
  public void setPublisher(RuleTaskAppendEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }
}
