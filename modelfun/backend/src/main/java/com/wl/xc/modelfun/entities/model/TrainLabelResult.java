package com.wl.xc.modelfun.entities.model;

import lombok.Data;

/**
 * 训练集标注结果
 *
 * @version 1.0
 * @date 2022/5/5 14:10
 */
@Data
public class TrainLabelResult {

  /**
   * 训练集标签类别数量
   */
  private Integer trainLabelCount;
  /**
   * 训练集标注语料量
   */
  private Long trainSentenceCount;
  /**
   * 未标注数据集覆盖率
   */
  private String unlabelCoverage;
  /**
   * 训练集文件路径
   */
  private String trainFile;

  private String testAccuracy;

  private String testRecall;

  private String testF1Score;
  /**
   * 测试集的打标文件地址
   */
  private String testLabelResult;

}
