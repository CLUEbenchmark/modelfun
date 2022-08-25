package com.wl.xc.modelfun.tasks.rule;

import static com.wl.xc.modelfun.commons.constants.CommonConstant.RULE_DELETE;
import static com.wl.xc.modelfun.commons.constants.CommonConstant.SESSION_UID;

import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import com.wl.xc.modelfun.service.RuleInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022.4.16 15:42
 */
@Slf4j
@Component
public class GlobalRuleTaskExecute implements RuleTaskExecute {

  private GlobalRuleParameterCalc globalRuleParameterCalc;

  private RuleInfoService ruleInfoService;

  @Override
  public RuleTaskType getRuleTaskType() {
    return RuleTaskType.GLOBAL;
  }

  @Override
  public void execute(RuleTask ruleTask) {
    Optional<Boolean> deleteOp = ruleTask
        .getConfig()
        .getByType(RULE_DELETE, Boolean.class);
    try {
      globalRuleParameterCalc.globalCalc(ruleTask);
      deleteOp.ifPresentOrElse(b -> deleteSuccessOp(ruleTask), () -> globalCalcSuccessOp(ruleTask));
    } catch (Exception e) {
      log.error("[GlobalRuleTaskExecute.execute] 全局规则任务执行失败，taskId={}", ruleTask.getTaskId(), e);
      deleteOp.ifPresentOrElse(b -> deleteFailedOp(ruleTask), () -> globalCalcFailedOp(ruleTask, e));
    }
  }

  private void deleteSuccessOp(RuleTask ruleTask) {
    RuleInfoPO ruleInfo = ruleTask.getRuleInfo();
    String uid = ruleTask.getConfig().getByType(SESSION_UID, String.class).orElse(null);
    ruleInfoService.deleteRuleById(ruleInfo.getId());
    WebsocketDTO websocketDTO = new WebsocketDTO();
    websocketDTO.setEvent(WsEventType.RULE_SUCCESS);
    websocketDTO.setData(
        WebsocketDataDTO.create(
            ruleTask.getTaskId(), "", "规则" + ruleInfo.getRuleName() + "删除成功", true));
    WebSocketHandler.sendByUid(uid, websocketDTO);
  }

  private void globalCalcSuccessOp(RuleTask ruleTask) {
    WebsocketDTO websocketDTO = new WebsocketDTO();
    websocketDTO.setEvent(WsEventType.RULE_SUCCESS);
    String uid = ruleTask.getConfig().getByType(SESSION_UID, String.class).orElse(null);
    websocketDTO.setData(
        WebsocketDataDTO.create(
            ruleTask.getTaskId(), "", "概览参数计算完成", true));
    WebSocketHandler.sendByUid(uid, websocketDTO);
  }

  private void deleteFailedOp(RuleTask ruleTask) {
    // 重置规则状态为完成
    RuleInfoPO ruleInfo = ruleTask.getRuleInfo();
    ruleInfo.setId(ruleInfo.getId());
    ruleInfo.setCompleted(0);
    ruleInfoService.updateByIdSelective(ruleInfo);
    String uid = ruleTask.getConfig().getByType(SESSION_UID, String.class).orElse(null);
    WebsocketDTO websocketDTO = new WebsocketDTO();
    websocketDTO.setEvent(WsEventType.RULE_FAIL);
    websocketDTO.setData(
        WebsocketDataDTO.create(
            ruleTask.getTaskId(),
            "",
            "规则" + ruleTask.getRuleInfo().getRuleName() + "删除失败",
            false));
    WebSocketHandler.sendByUid(uid, websocketDTO);
  }

  private void globalCalcFailedOp(RuleTask ruleTask, Exception e) {
    WebsocketDTO websocketDTO = new WebsocketDTO();
    websocketDTO.setEvent(WsEventType.RULE_FAIL);
    String uid = ruleTask.getConfig().getByType(SESSION_UID, String.class).orElse(null);
    String msg;
    if (e instanceof BusinessException) {
      msg = e.getMessage();
    } else {
      msg = "服务器内部错误，请联系管理员";
    }
    websocketDTO.setData(
        WebsocketDataDTO.create(
            ruleTask.getTaskId(), "", "概览参数计算失败，原因：" + msg, false));
    WebSocketHandler.sendByUid(uid, websocketDTO);
  }

  @Autowired
  public void setGlobalRuleParameterCalc(GlobalRuleParameterCalc globalRuleParameterCalc) {
    this.globalRuleParameterCalc = globalRuleParameterCalc;
  }

  @Autowired
  public void setRuleInfoService(RuleInfoService ruleInfoService) {
    this.ruleInfoService = ruleInfoService;
  }
}
