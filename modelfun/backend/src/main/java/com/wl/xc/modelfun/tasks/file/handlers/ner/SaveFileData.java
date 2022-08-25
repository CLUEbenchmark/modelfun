package com.wl.xc.modelfun.tasks.file.handlers.ner;

import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import java.io.File;

/**
 * @version 1.0
 * @date 2022/6/7 17:02
 */
@FunctionalInterface
public interface SaveFileData {

  void saveFileData(File file, int length, DatasetInfoPO po);
}
