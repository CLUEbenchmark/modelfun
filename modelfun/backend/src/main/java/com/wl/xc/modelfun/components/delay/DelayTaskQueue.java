package com.wl.xc.modelfun.components.delay;

import java.util.List;

/**
 * 延时队列,该延时队列中的每一个元素都有一个唯一的ID
 *
 * @version 1.0
 * @date 2022/7/1 15:08
 */
public interface DelayTaskQueue {

  String getQueueName();

  /**
   * <pre>
   * 把一个延时任务放入延时队列中，该任务必须有以下几个要求：
   * 1. 不能为null。
   * 2. jobId，topic，executeTime必须存在，不能为空。
   * </pre>
   *
   * @param delayJob 延时任务
   * @return 是否添加成功
   * @throws NullPointerException     如果delayJob为null，则抛出该异常
   * @throws IllegalArgumentException 如果delayJob的jobId，topic，executeTime为空，则抛出该异常
   */
  boolean add(DelayJob delayJob);

  /**
   * 根据任务id删除任务，如果任务不存在，则返回false。
   *
   * @param jobId 任务ID
   * @return 是否成功删除任务
   */
  boolean remove(String jobId);

  /**
   * 根据任务id删除任务，如果任务不存在，则返回false。
   *
   * @param jobIds 任务ID数组
   * @return 是否成功删除任务
   */
  boolean remove(String... jobIds);

  /**
   * 从延迟队列中，获取指定数量的任务，如果没有任务，则返回空列表。
   *
   * @param limit 获取的任务数量
   * @return 任务列表
   */
  List<DelayJob> poll(int limit);

  /**
   * 从延迟队列中获取一个任务。
   *
   * @return 任务
   */
  DelayJob poll();
}
