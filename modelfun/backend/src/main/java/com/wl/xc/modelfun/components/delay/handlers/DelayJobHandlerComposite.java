package com.wl.xc.modelfun.components.delay.handlers;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.components.delay.DelayJob;
import com.wl.xc.modelfun.components.delay.DelayJobHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有处理类的聚合
 *
 * @version 1.0
 * @author: Fan
 * @date 2021/11/10 18:41
 */
public class DelayJobHandlerComposite {

  private final List<DelayJobHandler> delegates = new ArrayList<>();

  public void addDelayJobHandler(List<DelayJobHandler> configurers) {
    if (configurers != null) {
      this.delegates.addAll(configurers);
    }
  }

  public void handle(DelayJob delayJob) {
    for (DelayJobHandler delegate : delegates) {
      if (delegate.canHandle(delayJob)) {
        delegate.handle(delayJob);
        break;
      }
    }
    throw new BusinessIllegalStateException("No handler can handle the delay job");
  }
}
