package com.wl.xc.modelfun.components;

import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.service.AlgorithmCallbackService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/6/29 13:29
 */
@Component
public class AlgorithmCallbackFactory {

  private final Map<CallBackAction, AlgorithmCallbackService> callbackServiceMap = new HashMap<>();

  public AlgorithmCallbackService getService(CallBackAction action) {
    return callbackServiceMap.getOrDefault(action, AlgorithmCallbackService.NULL_CALLBACK);
  }

  @Autowired(required = false)
  public void addServices(List<AlgorithmCallbackService> services) {
    if (services != null) {
      for (AlgorithmCallbackService service : services) {
        callbackServiceMap.put(service.getAction(), service);
      }
    }
  }
}
