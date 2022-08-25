package com.wl.xc.modelfun.tasks.file;

import com.wl.xc.modelfun.commons.WorkThreadFactory;
import com.wl.xc.modelfun.config.properties.FileUploadProperties;
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
public class FileTaskListener implements SmartLifecycle {

  private volatile boolean isRunning = false;

  private final LinkedBlockingQueue<FileTask> queue = new LinkedBlockingQueue<>();

  private final ThreadPoolExecutor main =
      new ThreadPoolExecutor(
          1,
          1,
          0L,
          TimeUnit.MILLISECONDS,
          new ArrayBlockingQueue<>(1),
          new WorkThreadFactory("file-main"));

  private final ThreadPoolExecutor fileTaskThread =
      new ThreadPoolExecutor(
          50,
          50,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(),
          new WorkThreadFactory("file-task"));

  private Semaphore semaphore;

  private FileUploadProperties fileUploadProperties;

  private FileTaskHandlerFactory fileTaskHandlerFactory;

  @EventListener(value = FileTaskAppendEvent.class)
  public void onFileTaskEvent(FileTaskAppendEvent event) {
    boolean result = queue.offer(event.getFileTask());
    if (!result) {
      log.error("[FileTaskListener.onFileTaskEvent] 插入队列失败，队列已满");
    }
  }

  private void run() {
    while (isRunning) {
      try {
        FileTask fileTask = queue.poll(10, TimeUnit.SECONDS);
        if (fileTask == null) {
          continue;
        }
        FileTaskHandler handler;
        try {
          handler = fileTaskHandlerFactory.getHandler(fileTask.getType());
        } catch (Exception e) {
          log.error("[FileTaskListener.run]", e);
          continue;
        }
        semaphore.acquire(1);
        fileTaskThread.execute(() -> handlerTask(handler, fileTask));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void handlerTask(FileTaskHandler handler, FileTask fileTask) {
    try {
      handler.handle(fileTask);
      handler.afterHandle(fileTask);
    } catch (Throwable e) {
      log.error("[FileTaskListener.handlerTask]", e);
    } finally {
      semaphore.release();
    }
  }

  @Override
  public void start() {
    Integer parallel = fileUploadProperties.getMaxParallelTask();
    semaphore = new Semaphore(parallel);
    main.execute(this::run);
    isRunning = true;
    log.info("[FileTaskListener.start] 文件任务开始执行");
  }

  @Override
  public void stop() {
    isRunning = false;
    fileTaskThread.shutdown();
    main.shutdown();
    log.info("[FileTaskListener.stop] 文件任务停止执行");
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
  public void setFileTaskHandlerFactory(FileTaskHandlerFactory fileTaskHandlerFactory) {
    this.fileTaskHandlerFactory = fileTaskHandlerFactory;
  }
}
