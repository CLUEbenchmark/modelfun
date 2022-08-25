package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickCacheKey;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickErrorKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.FewShotCallbackDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import com.wl.xc.modelfun.service.IntegrationService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 文本小样本学习回调处理类
 *
 * @version 1.0
 * @date 2022/6/29 14:11
 */
@Slf4j
@Service
public class TextFewShotCallbackService implements AlgorithmCallbackService {

  private IntegrationService integrationService;

  private StringRedisTemplate stringRedisTemplate;

  private TaskInfoService taskInfoService;

  private ObjectMapper objectMapper;

  @Override
  public CallBackAction getAction() {
    return CallBackAction.TEXT_FEW_SHOT;
  }

  @Override
  public ResultVo<Boolean> callback(String body) {
    FewShotCallbackDTO callbackDTO;
    try {
      callbackDTO = objectMapper.readValue(body, FewShotCallbackDTO.class);
    } catch (JsonProcessingException e) {
      log.error("[TextFewShotCallbackService.callback]", e);
      return ResultVo.create("回调请求体解析失败", -1, false, false);
    }
    Long taskId = callbackDTO.getTaskId();
    Long recordId = callbackDTO.getRecordId();
    if (callbackDTO.getState()) {
      log.info(
          "[IntegrationController.saveFewShotAsync] 收到文本小样本学习回调：taskId: {}, recordId:{}",
          taskId,
          recordId);
    } else {
      log.info(
          "[IntegrationController.saveFewShotAsync] 收到文本小样本学习回调: taskId: {}, recordId:{},错误信息：{}",
          taskId,
          recordId,
          callbackDTO.getDetail());
    }
    String key = RedisKeyMethods.getFewShowKey(taskId, recordId);
    String uid = stringRedisTemplate.opsForValue().get(key);
    ResultVo<Boolean> resultVo;
    String textClickCacheKey = getTextClickCacheKey(taskId, recordId);
    TaskInfoPO po = taskInfoService.getById(taskId);
    try {
      resultVo = integrationService.saveFewShotAsync(callbackDTO);
      boolean exitClick = Boolean.TRUE.equals(stringRedisTemplate.hasKey(textClickCacheKey));
      if (!resultVo.getData()) {
        if (!exitClick) {
          WebsocketDTO dto = new WebsocketDTO();
          dto.setEvent(WsEventType.INTEGRATED_FAIL);
          String msg = String.format("任务：%s集成失败，失败原因为：%s", po.getName(), resultVo.getMsg());
          dto.setData(WebsocketDataDTO.create(po.getId(), po.getName(), msg, false));
          WebSocketHandler.sendByUid(uid, dto);
        } else {
          // 如果是一键标注，并且发生错误信息，则缓存该信息给一键标注任务使用
          String errorKey = getTextClickErrorKey(taskId, recordId);
          stringRedisTemplate.opsForValue().set(errorKey, resultVo.getMsg(), 5, TimeUnit.MINUTES);
        }
      }
    } catch (Exception e) {
      WebsocketDTO dto = new WebsocketDTO();
      dto.setEvent(WsEventType.INTEGRATED_FAIL);
      String msg = String.format("任务：%s集成失败，失败原因为：%s", po.getName(), "服务器内部错误");
      dto.setData(WebsocketDataDTO.create(po.getId(), po.getName(), msg, false));
      WebSocketHandler.sendByUid(uid, dto);
      resultVo = ResultVo.create("服务器内部错误", -1, false, false);
    } finally {
      stringRedisTemplate.delete(key);
      stringRedisTemplate.delete(textClickCacheKey);
    }
    return resultVo;
  }

  @Autowired
  public void setIntegrationService(IntegrationService integrationService) {
    this.integrationService = integrationService;
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
