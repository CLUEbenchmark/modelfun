package com.wl.xc.modelfun.tasks.file.handlers;

import java.io.File;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/7 11:36
 */
@Data
public class PreCheckResult {

  /**
   * 是否存在训练集文件
   */
  private boolean hasTrainFile;
  /**
   * 是否存在测试集文件
   */
  private boolean hasTestFile;
  /**
   * 是否存在未标注集文件
   */
  private boolean hasUnlabelFile;
  /**
   * 是否存在标签集文件
   */
  private boolean hasLabelFile;
  /**
   * 测试集文件
   */
  private File testFile;
  /**
   * 测试集文件行数
   */
  private long testFileSize;
  /**
   * 训练集文件
   */
  private File trainFile;
  /**
   * 训练集文件行数
   */
  private long trainFileSize;
  /**
   * 未标注集文件
   */
  private File unlabelFile;
  /**
   * 未标注文件行数
   */
  private long unlabelFileSize;
  /**
   * 标签集文件
   */
  private File labelFile;
  /**
   * 标签集文件行数
   */
  private long labelFileSize;
  /**
   * 验证集
   */
  private File valFile;
  /**
   * 验证集行数
   */
  private long valFileSize;
}
