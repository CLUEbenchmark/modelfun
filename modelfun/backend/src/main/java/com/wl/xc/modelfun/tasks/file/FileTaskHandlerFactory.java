package com.wl.xc.modelfun.tasks.file;

import com.wl.xc.modelfun.commons.enums.FileTaskType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:23
 */
public class FileTaskHandlerFactory {

  private final Map<FileTaskType, FileTaskHandler> handlers = new HashMap<>();

  public void addHandlers(List<FileTaskHandler> fileTaskHandlers) {
    if (fileTaskHandlers != null && !fileTaskHandlers.isEmpty()) {
      for (FileTaskHandler fileTaskHandler : fileTaskHandlers) {
        handlers.put(fileTaskHandler.getType(), fileTaskHandler);
      }
    }
  }

  public FileTaskHandler getHandler(FileTaskType type) {
    return Optional.ofNullable(type)
        .flatMap(t -> Optional.ofNullable(handlers.get(t)))
        .orElseThrow(() -> new BusinessIllegalStateException("FileTaskHandler not found for type: " + type));
  }

}
