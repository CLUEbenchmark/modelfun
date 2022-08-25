package com.wl.xc.modelfun.service.impl;

import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskTrainKey;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickCacheKey;
import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTextClickErrorKey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.entities.dto.TrainCallbackDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import com.wl.xc.modelfun.service.ModelTrainService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 文本模型训练的回调处理类
 *
 * @version 1.0
 * @date 2022/6/29 14:38
 */
@Slf4j
@Service
public class TextModelTrainCallbackService implements AlgorithmCallbackService {

  private ModelTrainService modelTrainService;

  private StringRedisTemplate stringRedisTemplate;

  private TaskInfoService taskInfoService;

  private ObjectMapper objectMapper;

  @Override
  public CallBackAction getAction() {
    return CallBackAction.TEXT_MODEL_TRAIN;
  }

  @Override
  public ResultVo<Boolean> callback(String body) {
    TrainCallbackDTO trainCallbackDTO;
    try {
      trainCallbackDTO = objectMapper.readValue(body, TrainCallbackDTO.class);
    } catch (JsonProcessingException e) {
      log.error("[ModelTrainController.saveTrainResultAsync]", e);
      return ResultVo.create("解析失败", -1, false, false);
    }
    String key = getTaskTrainKey(trainCallbackDTO.getTaskId(), trainCallbackDTO.getRecordId());
    ResultVo<Boolean> resultVo;
    try {
      String uid = stringRedisTemplate.opsForValue().get(key);
      if (uid == null) {
        log.error("[ModelTrainController.saveTrainResultAsync] 模型训练任务已经超时: {}", key);
        return ResultVo.create("训练结果保存失败，模型训练任务已经超时", -1, false, false);
      }
      String textClickCacheKey =
          getTextClickCacheKey(trainCallbackDTO.getTaskId(), trainCallbackDTO.getRecordId());
      resultVo = modelTrainService.saveTrainResultAsync(trainCallbackDTO);
      TaskInfoPO po = taskInfoService.getById(trainCallbackDTO.getTaskId());
      boolean exitClick = Boolean.TRUE.equals(stringRedisTemplate.hasKey(textClickCacheKey));
      if (!resultVo.getData()) {
        if (!exitClick) {
          WebsocketDTO dto = new WebsocketDTO();
          dto.setEvent(WsEventType.TRAIN_FAIL);
          dto.setData(
              WebsocketDataDTO.create(po.getId(), po.getName(), trainCallbackDTO.getDetail(), false));
          WebSocketHandler.sendByUid(uid, dto);
        } else {
          // 如果是一键标注，并且发生错误信息，则缓存该信息给一键标注任务使用
          String errorKey =
              getTextClickErrorKey(trainCallbackDTO.getTaskId(), trainCallbackDTO.getRecordId());
          stringRedisTemplate
              .opsForValue()
              .set(errorKey, trainCallbackDTO.getDetail(), 5, TimeUnit.MINUTES);
          stringRedisTemplate.delete(textClickCacheKey);
        }
      } else {
        if (!exitClick) {
          // 丑陋的代码，用一个缓存表示是否是一键标注进行的训练
          // websocket通知
          WebsocketDTO dto = new WebsocketDTO();
          dto.setEvent(WsEventType.TRAIN_SUCCESS);
          dto.setData(WebsocketDataDTO.create(po.getId(), po.getName(), "模型训练成功", true));
          WebSocketHandler.sendByUid(uid, dto);
        } else {
          stringRedisTemplate.delete(textClickCacheKey);
        }
      }
    } finally {
      stringRedisTemplate.delete(key);
    }
    return resultVo;
  }

  @Autowired
  public void setModelTrainService(ModelTrainService modelTrainService) {
    this.modelTrainService = modelTrainService;
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
