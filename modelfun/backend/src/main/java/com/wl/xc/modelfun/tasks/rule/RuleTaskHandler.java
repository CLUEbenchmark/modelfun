package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;

/**
 * 规则运行任务的抽象类
 *
 * @version 1.0
 * @date 2022/4/15 13:17
 */
public interface RuleTaskHandler {

  RuleType getRuleType();

  void init();

  /**
   * 对语料进行打标签
   *
   * @param sentence 待打标签的语料
   * @return 标签
   */
  int label(String sentence, DatasetType datasetType);

  /**
   * 打完标签之后的处理
   */
  void afterLabel();

  void destroy();

}
