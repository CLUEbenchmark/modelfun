package com.wl.xc.modelfun.tasks.file;

import java.time.Clock;
import org.springframework.context.ApplicationEvent;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:44
 */
public class FileTaskAppendEvent extends ApplicationEvent {

  private final FileTask fileTask;

  public FileTaskAppendEvent(FileTask fileTask) {
    super(fileTask);
    this.fileTask = fileTask;
  }

  public FileTaskAppendEvent(FileTask fileTask, Clock clock) {
    super(fileTask, clock);
    this.fileTask = fileTask;
  }

  public FileTask getFileTask() {
    return fileTask;
  }

}
