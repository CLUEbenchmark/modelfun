package com.wl.xc.modelfun.tasks.daemon;

import static com.wl.xc.modelfun.commons.methods.RedisKeyMethods.getTaskTrainKey;

import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.commons.constants.FileCacheConstant;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.TrainCallbackDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.model.TrainCallbackEvent;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.service.ModelTrainService;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 一个简单的模型训练算法回调监听器，用于主动设置模型训练结果，如果超过指定时间算法仍没有回调，则认为是训练失败。
 * <p>
 * 由于是单一应用，目前简单写一下，一般来说，最好是由延时队列来完成。
 *
 * @version 1.0
 * @date 2022/5/11 12:26
 */
@Slf4j
@Component
public class TrainResultCallbackListener {

  private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10,
      new WorkThreadFactory("TrainResultCallbackListener"));

  private final Map<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<>(50);

  private StringRedisTemplate stringRedisTemplate;

  private ModelTrainService modelTrainService;

  private TaskInfoService taskInfoService;

  private OssService ossService;

  public TrainResultCallbackListener() {
    executor.setKeepAliveTime(60, TimeUnit.SECONDS);
    executor.setMaximumPoolSize(20);
    executor.allowCoreThreadTimeOut(true);
  }

  public void addCallbackListener(TrainCallbackEvent event) {
    ScheduledFuture<?> scheduledFuture =
        executor.schedule(
            new CallbackListener(event), event.getDelayMillisecond(), TimeUnit.MILLISECONDS);
    futures.put(event.getRecordId(), scheduledFuture);
  }

  public void removeCallbackListener(Long recordId) {
    ScheduledFuture<?> scheduledFuture = futures.get(recordId);
    if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
      scheduledFuture.cancel(true);
      futures.remove(recordId);
    }
  }

  private class CallbackListener implements Runnable {

    private final TrainCallbackEvent event;

    public CallbackListener(TrainCallbackEvent event) {
      this.event = event;
    }

    @Override
    public void run() {
      try {
        log.info("[CallbackListener.run] 模型训练任务超时：recordId={}", event.getRecordId());
        // 插入一条空白结果
        TrainCallbackDTO trainCallbackDTO = new TrainCallbackDTO();
        trainCallbackDTO.setTaskId(event.getTaskId());
        trainCallbackDTO.setRecordId(event.getRecordId());
        trainCallbackDTO.setStatus(3);
        modelTrainService.saveFailedTrainResult(trainCallbackDTO);
        // websocket通知
        TaskInfoPO po = taskInfoService.getById(event.getTaskId());
        WebsocketDTO dto = new WebsocketDTO();
        dto.setEvent(WsEventType.TRAIN_FAIL);
        dto.setData(WebsocketDataDTO.create(po.getId(), po.getName(), "模型训练任务超时", false));
        String key = getTaskTrainKey(event.getTaskId(), event.getRecordId());
        String uid = stringRedisTemplate.opsForValue().get(key);
        WebSocketHandler.sendByUid(uid, dto);
        // 删除redis缓存
        stringRedisTemplate.delete(key);
        // 删除缓存文件
        String cacheKey = RedisKeyMethods.getIntegrateFileCacheKey(event.getTaskId());
        String autoResult = (String) stringRedisTemplate.opsForHash()
            .get(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT);
        if (StringUtils.isNotBlank(autoResult)) {
          ossService.deleteFile(autoResult);
          stringRedisTemplate.opsForHash().delete(cacheKey, FileCacheConstant.INTEGRATE_AUTO_RESULT);
        }
      } catch (Exception e) {
        log.error("[CallbackListener.run]", e);
      }
    }
  }

  @Autowired
  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Autowired
  public void setModelTrainService(ModelTrainService modelTrainService) {
    this.modelTrainService = modelTrainService;
  }

  @Autowired
  public void setTaskInfoService(TaskInfoService taskInfoService) {
    this.taskInfoService = taskInfoService;
  }

  @Autowired
  public void setOssService(OssService ossService) {
    this.ossService = ossService;
  }
}
