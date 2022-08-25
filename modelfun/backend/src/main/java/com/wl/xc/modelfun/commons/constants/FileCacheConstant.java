package com.wl.xc.modelfun.commons.constants;

import lombok.Data;

/**
 * 文件地址缓存常量
 *
 * @version 1.0
 * @date 2022/5/17 10:23
 */
@Data
public class FileCacheConstant {

  public static final String INTEGRATE_FILE = "integrateFile";

  public static final String INTEGRATE_TEST_MATRIX = "testMatrix";

  public static final String INTEGRATE_UNLABEL_MATRIX = "unlabelMatrix";

  /**
   * 模型训练时，根据自动标注结果生成的文件
   */
  public static final String INTEGRATE_AUTO_RESULT = "autoResult";

  public static final String TRAIN_PATH = "trainFile";

}
