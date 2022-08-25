package com.wl.xc.modelfun.tasks.file;

import com.wl.xc.modelfun.commons.enums.FileTaskType;

/**
 * 处理文件任务的处理类
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:20
 */
public interface FileTaskHandler {

  FileTaskType getType();

  void handle(FileTask fileTask);

  void afterHandle(FileTask fileTask);

}
