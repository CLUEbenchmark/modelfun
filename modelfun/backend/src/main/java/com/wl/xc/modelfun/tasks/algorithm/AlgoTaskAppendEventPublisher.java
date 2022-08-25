package com.wl.xc.modelfun.tasks.algorithm;

import javax.annotation.Nonnull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022.4.16 16:00
 */
@Component
public class AlgoTaskAppendEventPublisher implements ApplicationEventPublisherAware {

  private ApplicationEventPublisher publisher;

  public void publish(AlgorithmTask algorithmTask) {
    publisher.publishEvent(new AlgoTaskAppendEvent(algorithmTask));
  }

  public void publish(AlgoTaskAppendEvent event) {
    publisher.publishEvent(event);
  }

  @Override
  public void setApplicationEventPublisher(@Nonnull ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }
}
