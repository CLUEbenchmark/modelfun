package com.wl.xc.modelfun.components.delay;

/**
 * 延时任务的处理类接口
 *
 * @version 1.0
 * @date 2022/7/4 14:32
 */
public interface DelayJobHandler {

  /**
   * 判断该处理类是否可以处理该任务
   *
   * @param delayJob 任务信息
   * @return true：可以处理；false：不能处理
   */
  boolean canHandle(DelayJob delayJob);

  /**
   * 处理任务
   *
   * @param delayJob 任务信息
   */
  void handle(DelayJob delayJob);
}
