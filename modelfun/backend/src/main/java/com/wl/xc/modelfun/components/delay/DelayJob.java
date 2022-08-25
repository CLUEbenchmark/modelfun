package com.wl.xc.modelfun.components.delay;

import lombok.Data;

/**
 * 每个任务，即Job都拥有以下属性：
 * <p>
 * topic：Job类型。可以理解成具体的业务名称。
 * <p>
 * Id：Job的唯一标识。用来检索和删除指定的Job信息。
 * <p>
 * executeTime: 执行的时间，以时间戳的形式保存，精确到毫秒的时间戳。
 * <p>
 * body：Job的内容，供消费者做具体的业务处理，以json格式存储。
 *
 * @version 1.0
 * @date 2022.7.2 14:00
 */
@Data
public class DelayJob {

  /**
   * Job的唯一标识。用来检索和删除指定的Job信息。
   */
  private String jobId;

  /**
   * Job类型。可以理解成具体的业务名称。
   */
  private String topic;

  /**
   * 执行的时间，以时间戳的形式保存，精确到毫秒的时间戳。
   */
  private long executeTime;

  /**
   * 任务执行的次数，可能有些任务会因为执行次数的增长有不同的操作。
   */
  private int executeCount;

  /**
   * Job的内容，供消费者做具体的业务处理，以json格式存储。
   */
  private String body;
}