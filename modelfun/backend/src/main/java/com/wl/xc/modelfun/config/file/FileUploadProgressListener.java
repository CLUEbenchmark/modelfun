package com.wl.xc.modelfun.config.file;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.ProgressListener;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @version 1.0
 * @date 2022/3/28 22:32
 */
@Slf4j
public class FileUploadProgressListener implements ProgressListener {

  private static final ThreadLocal<String> CURRENT_TASK_ID = new ThreadLocal<>();

  private StringRedisTemplate stringRedisTemplate;

  @Override
  public void update(long pBytesRead, long pContentLength, int pItems) {
    String taskId = CURRENT_TASK_ID.get();
    if (taskId == null) {
      return;
    }
    double percent = pBytesRead * 100.0 / pContentLength / 4;
    if (percent < 0) {
      percent = 0;
    } else if (percent > 100) {
      percent = 100;
    }
    log.info("[FileUploadProgressListener.update] taskId: {}, percent: {}", taskId, percent);
    try {
      stringRedisTemplate.opsForValue().set(taskId, String.valueOf(percent), 10, TimeUnit.MINUTES);
    } catch (Exception e) {
      log.error("[FileUploadProgressListener.update]", e);
    }
  }

  public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  public void setCurrentTaskId(String taskId) {
    CURRENT_TASK_ID.set(taskId);
  }

  public void removeCurrentTaskId() {
    CURRENT_TASK_ID.remove();
  }

}
