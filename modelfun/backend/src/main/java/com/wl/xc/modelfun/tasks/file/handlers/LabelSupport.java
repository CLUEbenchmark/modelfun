package com.wl.xc.modelfun.tasks.file.handlers;

import java.util.Map;

/**
 * @version 1.0
 * @date 2022/6/7 17:22
 */
public class LabelSupport {

  private static final ThreadLocal<Map<String, Integer>> LABEL_HOLDER = new ThreadLocal<>();

  public static void setCurrentLabel(Map<String, Integer> labelMap) {
    LABEL_HOLDER.set(labelMap);
  }

  public static Map<String, Integer> getCurrentLabel() {
    return LABEL_HOLDER.get();
  }

  public static void removeCurrentLabel() {
    LABEL_HOLDER.remove();
  }

}
