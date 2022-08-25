package com.wl.xc.modelfun.commons.methods;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @version 1.0
 * @date 2022/6/21 17:03
 */
public class FileMethods {

  public static Path prepareFile(String parentPath, String fileName) {
    Path path = Paths.get(parentPath, fileName);
    File parent = path.getParent().toFile();
    if (!parent.exists()) {
      boolean result = parent.mkdirs();
      if (!result) {
        throw new BusinessIllegalStateException("创建本地临时文件夹失败");
      }
    }
    File file = path.toFile();
    file.deleteOnExit();
    return path;
  }

}
