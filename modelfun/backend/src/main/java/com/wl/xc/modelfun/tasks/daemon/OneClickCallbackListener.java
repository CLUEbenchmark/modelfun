package com.wl.xc.modelfun.tasks.daemon;

import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.commons.enums.WsEventType;
import com.wl.xc.modelfun.commons.methods.RedisKeyMethods;
import com.wl.xc.modelfun.entities.dto.CallbackEventDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDTO;
import com.wl.xc.modelfun.entities.dto.WebsocketDataDTO;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import com.wl.xc.modelfun.service.IntegrationRecordsService;
import com.wl.xc.modelfun.service.TaskInfoService;
import com.wl.xc.modelfun.websocket.WebSocketHandler;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 一键标注的回调监听器。
 *
 * <p>正常的做法是应该设计一个总的回调监听框架，然后使用一些中间件来实现延时队列，防止因为重启应用导致内存中的任务丢失。
 *
 * <p>并且应该在这个统一的框架下，根据不同的业务类型进行不同的回调监听器，但是时间紧任务急，就先这样吧。。。
 *
 * @version 1.0
 * @date 2022/5/26 13:45
 */
@Slf4j
@Component
public class OneClickCallbackListener {

  private final ScheduledThreadPoolExecutor executor =
      new ScheduledThreadPoolExecutor(10, new WorkThreadFactory("OneClickCallbackListener"));

  private final Map<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<>(50);

  private StringRedisTemplate stringRedisTemplate;

  private TaskInfoService taskInfoService;

  private IntegrationRecordsService integrationRecordsService;

  public OneClickCallbackListener() {
    executor.setKeepAliveTime(60, TimeUnit.SECONDS);
    executor.setMaximumPoolSize(20);
    executor.allowCoreThreadTimeOut(true);
  }

  public void addCallbackListener(CallbackEventDTO event) {
    long delay = event.getExecuteTime() - System.currentTimeMillis();
    String cacheKey = RedisKeyMethods.getOneClickCacheKey(event.getTaskId());
    Boolean hasKey = stringRedisTemplate.hasKey(cacheKey);
    if (!Boolean.TRUE.equals(hasKey)) {
      log.warn(
          "[OneClickCallbackListener.addCallbackListener] 任务[{}]的回调事件已经被取消，不再添加回调监听器",
          event.getTaskId());
      return;
    }
    ScheduledFuture<?> scheduledFuture =
        executor.schedule(
            new OneClickCallbackRunner(event), Math.max(0, delay), TimeUnit.MILLISECONDS);
    futures.put(event.getTaskId(), scheduledFuture);
  }

  public void removeCallbackListener(Long taskId) {
    ScheduledFuture<?> scheduledFuture = futures.get(taskId);
    if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
      scheduledFuture.cancel(true);
      futures.remove(taskId);
    }
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

  private class OneClickCallbackRunner implements Runnable {

    private final CallbackEventDTO event;

    public OneClickCallbackRunner(CallbackEventDTO event) {
      this.event = event;
    }

    @Override
    public void run() {
      log.info("[OneClickCallbackRunner.run] 一键标注回调超时，taskId={}", event.getTaskId());
      String cacheKey = RedisKeyMethods.getOneClickCacheKey(event.getTaskId());
      // websocket通知
      TaskInfoPO po = taskInfoService.getById(event.getTaskId());
      WebsocketDTO dto = new WebsocketDTO();
      dto.setEvent(WsEventType.ONE_CLICK_FAIL);
      dto.setData(WebsocketDataDTO.create(po.getId(), po.getName(), "一键标注任务超时", false));
      String uid = stringRedisTemplate.opsForValue().get(cacheKey);
      WebSocketHandler.sendByUid(uid, dto);
      // 删除缓存
      stringRedisTemplate.delete(cacheKey);
      IntegrationRecordsPO recordsPO = new IntegrationRecordsPO();
      recordsPO.setId(event.getRecordId());
      recordsPO.setIntegrateStatus(2);
      recordsPO.setUpdateDatetime(LocalDateTime.now());
      integrationRecordsService.updateById(recordsPO);
    }
  }
}
