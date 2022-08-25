package com.wl.xc.modelfun.components.delay.impl;

import com.wl.xc.modelfun.components.delay.DelayJob;
import com.wl.xc.modelfun.components.delay.DelayTaskQueue;
import com.wl.xc.modelfun.components.delay.handlers.DelayJobHandlerComposite;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonObject;
import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;

/**
 * 用于监听Redisson的DelayQueue中是否存在到执行时间任务的监听器。
 *
 * <p>使用简单的轮询去查询就绪队列中的任务。本来想使用Redisson的订阅发布来实现，但是查看源码发现，只有当插入元素在队头时，才会发布消息。
 * 这样会导致后加入的任务执行不了。不使用Redisson的延时队列自己写一个倒是可以。
 *
 * @version 1.0
 * @date 2022/7/2 23:23
 */
@Slf4j
public class RedissonDelayTaskQueueWatcher {

  private static final String SCRIPT =
      "local src = KEYS[1]\n"
          + "local des = KEYS[2]\n"
          + "local value\n"
          + "while tonumber(redis.call('LLEN', src)) > 0 do\n"
          + "  value = redis.call('LPOP', src)\n"
          + "  if value then\n"
          + "    redis.call('LPUSH', des, value)\n"
          + "  end\n"
          + "end\n"
          + "return true";

  private final DelayTaskQueue delayTaskQueue;

  private final RedissonClient redissonClient;

  private final RQueue<String> tempQueue;

  private final RMap<String, String> jobMap;

  private volatile boolean running = false;

  private DelayJobHandlerComposite delayJobHandlerComposite;

  private final ThreadPoolExecutor threadPoolExecutor =
      new ThreadPoolExecutor(
          1, 1, 0L, TimeUnit.MILLISECONDS, new java.util.concurrent.LinkedBlockingQueue<>());

  public RedissonDelayTaskQueueWatcher(
      DelayTaskQueue delayTaskQueue, RedissonClient redissonClient) {
    this.delayTaskQueue = delayTaskQueue;
    this.redissonClient = redissonClient;
    String tempQueueName = RedissonObject.prefixName(delayTaskQueue.getQueueName(), "temp");
    String jobMapName = RedissonObject.prefixName(delayTaskQueue.getQueueName(), "jobMap");
    this.jobMap = redissonClient.getMap(jobMapName);
    tempQueue = this.redissonClient.getQueue(tempQueueName);
  }

  public void start() {
    synchronized (this) {
      if (running) {
        return;
      }
      running = true;
      redissonClient
          .getScript()
          .eval(
              Mode.READ_WRITE,
              SCRIPT,
              ReturnType.BOOLEAN,
              Arrays.asList(tempQueue.getName(), delayTaskQueue.getQueueName()));
      threadPoolExecutor.execute(this::run);
    }
  }

  private void run() {
    while (running) {
      try {
        List<DelayJob> jobs = delayTaskQueue.poll(20);
        // 处理接收到的任务
        handleTask(jobs);
        try {
          // 等待300ms
          TimeUnit.MILLISECONDS.sleep(300);
        } catch (InterruptedException e) {
          log.error("[QueueWatcher.poll]", e);
          Thread.currentThread().interrupt();
        }
      } catch (Throwable e) {
        log.error("[RedissonDelayTaskQueueWatcher.poll]", e);
      }
    }
    log.info("[RedissonDelayTaskQueueWatcher.poll] 结束轮询");
  }

  public void handleTask(List<DelayJob> jobList) {
    for (DelayJob delayJob : jobList) {
      try {
        delayJobHandlerComposite.handle(delayJob);
      } catch (Exception e) {
        log.error("[RedissonDelayTaskQueueWatcher.handleTask] 处理任务中出现错误", e);
      } finally {
        // 最后需要移除临时队列中的任务
        jobMap.remove(delayJob.getJobId());
        tempQueue.remove(delayJob.getJobId());
      }
    }
  }

  public void stop() {
    running = false;
    threadPoolExecutor.shutdown();
  }
}
