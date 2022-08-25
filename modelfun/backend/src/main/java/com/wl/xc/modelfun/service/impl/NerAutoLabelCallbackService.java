package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskLabelKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.entities.dto.NerAutoLabelCallbackDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.NerService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * ner自动标注的回调处理类
 *
 * @version 1.0
 * @date 2022/6/29 14:47
 */
@Slf4j
@Service
public class NerAutoLabelCallbackService implements AlgorithmCallbackService {

  private StringRedisTemplate stringRedisTemplate;

  private TaskInfoService taskInfoService;

  private IntegrationRecordsService integrationRecordsService;

  private NerService nerService;

  private ObjectMapper objectMapper;

  @Override
  public CallBackAction getAction() {
    return CallBackAction.NER_AUTO_LABEL;
  }

  @Override
  public ResultVo<Boolean> callback(String body) {
    NerAutoLabelCallbackDTO dto;
    try {
      dto = objectMapper.readValue(body, NerAutoLabelCallbackDTO.class);
    } catch (JsonProcessingException e) {
      log.error("[NerController.saveAutoAsync]", e);
      return ResultVo.create("解析异步返回的自动标注结果失败", -1, false, false);
    }
    log.info(
        "[NerController.saveAutoAsync] 收到ner自动标注回调：taskId={}, recordId={}, detail={}",
        dto.getTaskId(),
        dto.getRecordId(),
        dto.getDetail());
    String key = getTaskLabelKey(dto.getTaskId(), dto.getRecordId());
    String uid = stringRedisTemplate.opsForValue().get(key);
    if (StringUtils.isBlank(uid)) {
      return ResultVo.create("任务超时", -1, false, false);
    }
    stringRedisTemplate.delete(key);
    ResultVo<Boolean> resultVo;
    try {
      resultVo = nerService.saveAutoAsync(dto);
    } catch (BusinessException e) {
      IntegrationRecordsPO p = new IntegrationRecordsPO();
      p.setLabeled(3);
      p.setId(dto.getRecordId());
      p.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsService.updateById(p);
      resultVo = ResultVo.create(e.getMessage(), -1, false, false);
    }
    TaskInfoPO po = taskInfoService.getById(dto.getTaskId());
    WebsocketDTO websocketDTO = new WebsocketDTO();
    if (!resultVo.getData()) {
      websocketDTO.setEvent(WsEventType.AUTO_LABEL_FAIL);
      websocketDTO.setData(
          WebsocketDataDTO.create(po.getId(), po.getName(), dto.getDetail(), false));
    } else {
      websocketDTO.setEvent(WsEventType.AUTO_LABEL_SUCCESS);
      websocketDTO.setData(WebsocketDataDTO.create(po.getId(), po.getName(), "NER自动标注成功", true));
    }
    WebSocketHandler.sendByUid(uid, websocketDTO);
    return resultVo;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
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
  public void setIntegrationRecordsService(IntegrationRecordsService integrationRecordsService) {
    this.integrationRecordsService = integrationRecordsService;
  }

  @Autowired
  public void setNerService(NerService nerService) {
    this.nerService = nerService;
  }
}
