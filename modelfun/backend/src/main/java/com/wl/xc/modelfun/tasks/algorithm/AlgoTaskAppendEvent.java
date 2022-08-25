package com.wl.xc.modelfun.tasks.algorithm;

import java.time.Clock;
import org.springframework.context.ApplicationEvent;

/**
 * @version 1.0
 * @date 2022/4/20 11:04
 */
public class AlgoTaskAppendEvent extends ApplicationEvent {

  private final AlgorithmTask algorithmTask;

  public AlgoTaskAppendEvent(AlgorithmTask source) {
    super(source);
    this.algorithmTask = source;
  }

  public AlgoTaskAppendEvent(AlgorithmTask source, Clock clock) {
    super(source, clock);
    this.algorithmTask = source;
  }

  public AlgorithmTask getAlgorithmTask() {
    return algorithmTask;
  }
}
