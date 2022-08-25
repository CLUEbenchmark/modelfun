package com.wl.xc.modelfun.components.delay.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.components.delay.DelayJob;
import com.wl.xc.modelfun.components.delay.DelayTaskQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonObject;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RMap;
import org.redisson.api.RQueue;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;

/**
 * Redisson实现的延时队列。其内部使用Redisson的RDelayQueue实现。
 *
 * <pre>
 * 具体流程为，当把任务延时队列中时，Redisson会先把任务放入zset中，根据执行时间进行排序，然后把最近的任务执行时间，
 * 使用redis的发布订阅功能发送。然后订阅该通道的任务执行器会使用netty的时间轮进生成一个schedule任务，
 * 当达到执行时间时，把延时队列中的元素移到指定的队列中（即就绪队列，使用rpush），并从zset中移除。
 * </pre>
 *
 * <p>需要注意的是，Redisson的延时队列，其内部是由两个结构组成的，一个是zset，一个是list。list是用来按照顺序存放元素。
 *
 * <p>在这里，使用Redisson的延时队列存储任务的jobId，额外使用一个map结构来存储具体的任务信息。
 *
 * @version 1.0
 * @date 2022.7.2 15:01
 */
@Slf4j
public class RedissonDelayTaskQueue implements DelayTaskQueue {

  /**
   * Redisson客户端。
   */
  private final RedissonClient redissonClient;

  private final ObjectMapper objectMapper;
  /**
   * 存储jobId和DelayJob映射的map。
   */
  private final RMap<String, String> jobMap;
  /**
   * 延时队列的名称。
   */
  private final String queueName;

  private final String jobMapName;

  private final String tempQueueName;

  private final RDelayedQueue<String> delayedQueue;

  public RedissonDelayTaskQueue(RedissonClient redissonClient, String queueName, ObjectMapper objectMapper) {
    this.queueName = queueName;
    this.redissonClient = redissonClient;
    this.objectMapper = objectMapper;
    // 初始化jobMap
    jobMapName = RedissonObject.prefixName(queueName, "jobMap");
    jobMap = redissonClient.getMap(jobMapName);
    // 初始化readyQueue
    RQueue<String> readyQueue = redissonClient.getQueue(queueName);
    // 初始化delayedQueue
    delayedQueue = redissonClient.getDelayedQueue(readyQueue);
    tempQueueName = RedissonObject.prefixName(queueName, "temp");
  }

  @Override
  public String getQueueName() {
    return queueName;
  }

  /**
   * 当添加元素时，先把对应的job信息存入到map中，然后把对应的jobId存入到delayQueue中。
   *
   * @param delayJob 延时任务
   * @return 是否添加成功
   */
  @Override
  public boolean add(DelayJob delayJob) {
    Objects.requireNonNull(delayJob, "delayJob不能为null");
    if (delayJob.getJobId() == null || delayJob.getJobId().isEmpty()) {
      throw new IllegalArgumentException("jobId不能为空");
    }
    if (delayJob.getTopic() == null || delayJob.getTopic().isEmpty()) {
      throw new IllegalArgumentException("topic不能为空");
    }
    long delay = delayJob.getExecuteTime() - System.currentTimeMillis();
    if (delay <= 0) {
      throw new IllegalArgumentException("执行时间必须大于当前时间");
    }
    String value;
    try {
      value = objectMapper.writeValueAsString(delayJob);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
    jobMap.put(delayJob.getJobId(), value);
    delayedQueue.offer(delayJob.getJobId(), delay, TimeUnit.MILLISECONDS);
    return true;
  }

  /**
   * 从延时队列中移除指定的jobId。如果该jobId不存在延时队列中，则返回false。
   *
   * @param jobId 任务ID
   * @return 是否删除成功
   */
  @Override
  public boolean remove(String jobId) {
    String delayJob = jobMap.remove(jobId);
    if (delayJob == null) {
      return false;
    }
    return delayedQueue.remove(jobId);
  }

  @Override
  public boolean remove(String... jobIds) {
    long remove = jobMap.fastRemove(jobIds);
    if (remove > 0) {
      return delayedQueue.removeAll(Arrays.asList(jobIds));
    }
    return false;
  }

  @Override
  public List<DelayJob> poll(int limit) {
    if (limit <= 0) {
      return Collections.emptyList();
    }
    List<String> delayJobs = redissonClient
        .getScript()
        .eval(
            Mode.READ_WRITE,
            "local result = {};\n"
                + "for i = 1, ARGV[1], 1 do \n"
                + "  local jobId = redis.call('lpop', KEYS[1]);\n"
                + "  if jobId ~= false then \n"
                + "    if redis.call('hexists', KEYS[3], jobId) ~= 0 then \n"
                + "      local job = redis.call('hget', KEYS[3], jobId);\n"
                + "      table.insert(result, job);\n"
                + "      table.insert(result, jobId);\n"
                + "      redis.call('rpush', KEYS[2], jobId);\n"
                + "    else \n"
                + "      redis.call('hdel', KEYS[3], jobId);\n"
                + "    end;\n"
                + "  else \n"
                + "    return result;\n"
                + "  end;\n"
                + "end;\n"
                + "return result;",
            ReturnType.MULTI,
            Arrays.asList(queueName, tempQueueName, jobMapName),
            limit);
    if (delayJobs == null || delayJobs.isEmpty()) {
      return Collections.emptyList();
    }
    int size = delayJobs.size();
    List<DelayJob> result = new ArrayList<>(size);
    for (int i = 0; i < size; i = i + 2) {
      String delayJob = delayJobs.get(i);
      String jobId = delayJobs.get(i + 1);
      DelayJob job;
      try {
        job = objectMapper.readValue(delayJob, DelayJob.class);
      } catch (JsonProcessingException e) {
        // 如果出现json解析异常，说明redis中的数据有问题，一般是认为塞入才会这样
        // 从临时队列和jobMap中删除该jobId
        jobMap.remove(jobId);
        redissonClient.getQueue(tempQueueName).remove(jobId);
        log.error("[RedissonDelayTaskQueue.poll] 解析任务出错", e);
        continue;
      }
      result.add(job);
    }
    return result;
  }

  /**
   * 把就绪队列中的第一个元素移到临时队列中。
   *
   * <p>这里的poll方法并不是单纯的从就绪队列中取出第一个元素，因为考虑到任务的持久化问题。如果直接从就绪队列中取出，那么该任务就仅存在服务内存中，
   * 当服务由于意外原因停止并且任务尚未执行完毕，那么该任务就再没重试的机会。
   *
   * @return 就绪队列中的第一个元素
   */
  @Override
  public DelayJob poll() {
    // 由于Redisson延时队列是使用rpush把元素放入队列的，但是redis没有lpoprpush命令，所以这里只能使用脚本操作。
    List<DelayJob> poll = poll(1);
    if (poll.isEmpty()) {
      return null;
    }
    return poll.get(0);
  }
}
