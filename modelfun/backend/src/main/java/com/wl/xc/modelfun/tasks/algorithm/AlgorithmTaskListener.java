package com.wl.xc.modelfun.tasks.algorithm;

import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 算法交互任务监听器
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:43
 */
@Slf4j
@Component
public class AlgorithmTaskListener implements SmartLifecycle {

  private volatile boolean isRunning = false;

  private final LinkedBlockingQueue<AlgorithmTask> queue = new LinkedBlockingQueue<>();

  private final List<AlgorithmHandler> handlers = new ArrayList<>();

  private final ThreadPoolExecutor main =
      new ThreadPoolExecutor(
          1,
          1,
          0L,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(1),
          new WorkThreadFactory("algo-main"));

  private final ThreadPoolExecutor algoTaskThread =
      new ThreadPoolExecutor(
          50,
          50,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(),
          new WorkThreadFactory("algo-task"));

  private Semaphore semaphore;

  private FileUploadProperties fileUploadProperties;

  @EventListener(value = AlgoTaskAppendEvent.class)
  public void onFileTaskEvent(AlgoTaskAppendEvent event) {
    boolean result = queue.offer(event.getAlgorithmTask());
    if (!result) {
      log.error("[AlgorithmTaskListener.onFileTaskEvent] 插入队列失败，队列已满");
    }
  }

  private void run() {
    while (isRunning) {
      try {
        AlgorithmTask algorithmTask = queue.poll(10, TimeUnit.SECONDS);
        if (algorithmTask == null) {
          continue;
        }
        try {
          AlgorithmHandler handler = getHandler(algorithmTask.getType());
          semaphore.acquire(1);
          algoTaskThread.execute(() -> handlerTask(handler, algorithmTask));
        } catch (Exception e) {
          log.error("[AlgorithmTaskListener.run]", e);
        }
      } catch (InterruptedException e) {
        log.error("[AlgorithmTaskListener.run]", e);
      }
    }
  }

  private void handlerTask(AlgorithmHandler handler, AlgorithmTask algorithmTask) {
    try {
      handler.handle(algorithmTask);
    } catch (Throwable e) {
      log.error("[AlgorithmTaskListener.handlerTask]", e);
    } finally {
      semaphore.release();
    }
  }

  private AlgorithmHandler getHandler(AlgorithmTaskType taskType) {
    for (AlgorithmHandler handler : handlers) {
      if (handler.getType().equals(taskType)) {
        return handler;
      }
    }
    throw new BusinessIllegalStateException("错误的任务类型！");
  }

  @Override
  public void start() {
    Integer parallel = fileUploadProperties.getMaxParallelTask();
    semaphore = new Semaphore(parallel);
    main.execute(this::run);
    isRunning = true;
    log.info("[AlgorithmTaskListener.start] 规则任务开始执行");
  }

  @Override
  public void stop() {
    isRunning = false;
    algoTaskThread.shutdown();
    main.shutdown();
    log.info("[AlgorithmTaskListener.stop] 规则任务停止执行");
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }

  @Autowired
  public void addHandler(List<AlgorithmHandler> handler) {
    handlers.clear();
    handlers.addAll(handler);
  }
}
