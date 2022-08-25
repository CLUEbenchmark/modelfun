package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskTrainKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.entities.dto.NerTrainCallbackDTO;
import com.wl.xc.modelfun.entities.dto.TrainCallbackDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import com.wl.xc.modelfun.service.ModelTrainService;
import com.wl.xc.modelfun.service.NerService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/6/29 14:55
 */
@Slf4j
@Service
public class NerTrainCallbackService implements AlgorithmCallbackService {

  private NerService nerService;

  private StringRedisTemplate stringRedisTemplate;

  private TaskInfoService taskInfoService;

  private ModelTrainService modelTrainService;

  private ObjectMapper objectMapper;


  @Override
  public CallBackAction getAction() {
    return CallBackAction.NER_TRAIN;
  }

  @Override
  public ResultVo<Boolean> callback(String body) {
    NerTrainCallbackDTO dto;
    try {
      dto = objectMapper.readValue(body, NerTrainCallbackDTO.class);
    } catch (JsonProcessingException e) {
      log.error("[NerTrainCallbackService.callback]", e);
      return ResultVo.create("回调请求体解析失败", -1, false, false);
    }
    String key = getTaskTrainKey(dto.getTaskId(), dto.getRecordId());
    ResultVo<Boolean> resultVo;
    try {
      String uid = stringRedisTemplate.opsForValue().get(key);
      if (StringUtils.isBlank(uid)) {
        return ResultVo.create("任务超时", -1, false, false);
      }
      try {
        log.info(
            "[NerController.saveTrainAsync] 收到ner模型训练回调：taskId={}, recordId={}, detail={}",
            dto.getTaskId(),
            dto.getRecordId(),
            dto.getDetail());
        log.info(
            "[NerController.saveTrainAsync] precision={}, recall={}, f1={}, accuracy={}",
            dto.getResults().getPrecision(),
            dto.getResults().getRecall(),
            dto.getResults().getFscore(),
            dto.getResults().getAccuracy());
        resultVo = nerService.saveTrainAsync(dto);
      } catch (Exception e) {
        log.error("[NerController.saveTrainAsync]", e);
        TrainCallbackDTO trainCallbackDTO = new TrainCallbackDTO();
        trainCallbackDTO.setTaskId(dto.getTaskId());
        trainCallbackDTO.setRecordId(dto.getRecordId());
        modelTrainService.saveFailedTrainResult(trainCallbackDTO);
        resultVo = ResultVo.create("服务器内部错误", -1, false, false);
      }
      TaskInfoPO po = taskInfoService.getById(dto.getTaskId());
      WebsocketDTO websocketDTO = new WebsocketDTO();
      if (!resultVo.getData()) {
        websocketDTO.setEvent(WsEventType.TRAIN_FAIL);
        websocketDTO.setData(
            WebsocketDataDTO.create(po.getId(), po.getName(), dto.getDetail(), false));
      } else {
        websocketDTO.setEvent(WsEventType.TRAIN_SUCCESS);
        websocketDTO.setData(WebsocketDataDTO.create(po.getId(), po.getName(), "模型训练成功", true));
      }
      WebSocketHandler.sendByUid(uid, websocketDTO);
    } finally {
      stringRedisTemplate.delete(key);
    }
    return resultVo;
  }

  @Autowired
  public void setNerService(NerService nerService) {
    this.nerService = nerService;
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
  public void setModelTrainService(ModelTrainService modelTrainService) {
    this.modelTrainService = modelTrainService;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
