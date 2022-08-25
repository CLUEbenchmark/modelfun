package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
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
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:43
 */
@Slf4j
@Component
public class RuleTaskListener implements SmartLifecycle {

  private volatile boolean isRunning = false;

  private final LinkedBlockingQueue<RuleTask> queue = new LinkedBlockingQueue<>();

  private final ThreadPoolExecutor main =
      new ThreadPoolExecutor(
          1,
          1,
          0L,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(1),
          new WorkThreadFactory("rule-main"));

  private final ThreadPoolExecutor ruleTaskThread =
      new ThreadPoolExecutor(
          50,
          50,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(),
          new WorkThreadFactory("rule-task"));

  private Semaphore semaphore;

  private FileUploadProperties fileUploadProperties;

  private List<RuleTaskExecute> ruleTaskExecutes;

  @EventListener(value = RuleTaskAppendEvent.class)
  public void onFileTaskEvent(RuleTaskAppendEvent event) {
    boolean result = queue.offer(event.getRuleTask());
    if (!result) {
      log.error("[RuleTaskListener.onFileTaskEvent] 插入队列失败，队列已满");
    }
  }

  private void run() {
    while (isRunning) {
      try {
        RuleTask ruleTask = queue.poll(10, TimeUnit.SECONDS);
        if (ruleTask == null) {
          continue;
        }
        RuleTaskExecute taskExecute;
        try {
          taskExecute = getExecute(ruleTask.getType());
        } catch (Exception e) {
          log.error("[RuleTaskListener.run]", e);
          continue;
        }
        semaphore.acquire(1);
        ruleTaskThread.execute(() -> handlerTask(taskExecute, ruleTask));
      } catch (InterruptedException e) {
        log.error("[RuleTaskListener.run]", e);
      }
    }
  }

  private void handlerTask(RuleTaskExecute handler, RuleTask ruleTask) {
    try {
      handler.execute(ruleTask);
    } catch (Throwable e) {
      log.error("[RuleTaskListener.handlerTask]", e);
    } finally {
      semaphore.release();
    }
  }

  private RuleTaskExecute getExecute(RuleTaskType ruleTaskType) {
    for (RuleTaskExecute execute : ruleTaskExecutes) {
      if (execute.getRuleTaskType() == ruleTaskType) {
        return execute;
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
    log.info("[RuleTaskListener.start] 规则任务开始执行");
  }

  @Override
  public void stop() {
    isRunning = false;
    ruleTaskThread.shutdown();
    main.shutdown();
    log.info("[RuleTaskListener.stop] 规则任务停止执行");
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Autowired
  public void setRuleTaskExecutes(List<RuleTaskExecute> ruleTaskExecutes) {
    this.ruleTaskExecutes = ruleTaskExecutes;
  }

  @Autowired
  public void setFileUploadProperties(FileUploadProperties fileUploadProperties) {
    this.fileUploadProperties = fileUploadProperties;
  }
}
