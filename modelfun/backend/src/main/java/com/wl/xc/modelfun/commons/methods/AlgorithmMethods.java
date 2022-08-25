package com.wl.xc.modelfun.commons.methods;

import com.wl.xc.modelfun.commons.enums.CallBackAction;

/**
 * @version 1.0
 * @date 2022/6/29 14:33
 */
public class AlgorithmMethods {

  public static String generateUrl(String rawUrl, CallBackAction action) {
    return rawUrl + "?action=" + action.getType();
  }

}
