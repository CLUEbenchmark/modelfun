package com.wl.xc.modelfun.tasks.file.handlers;


import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.service.OssService;
import com.wl.xc.modelfun.tasks.file.FileTask;
import com.wl.xc.modelfun.tasks.file.FileTaskHandler;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/4/14 10:56
 */
@Component
public class FileDeleteHandler implements FileTaskHandler {

  private OssService ossService;

  @Override
  public FileTaskType getType() {
    return FileTaskType.OSS_DEL;
  }

  @Override
  public void handle(FileTask fileTask) {
    String path = fileTask.getPath();
    ossService.deleteFile(path);
  }

  @Override
  public void afterHandle(FileTask fileTask) {

  }


}
