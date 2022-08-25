package com.wl.xc.modelfun.service;

import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.vo.ResultVo;

/**
 * @version 1.0
 * @date 2022/6/29 13:29
 */
public interface AlgorithmCallbackService {

  AlgorithmCallbackService NULL_CALLBACK = new DefaultNullCallbackService();

  /**
   * 由于每个回调处理类对应的回调action总是只有一个，所以不使用类似canHandle之类的方式。
   *
   * @return 回调action
   */
  CallBackAction getAction();

  /**
   * 处理回调请求
   *
   * @param body 回调请求体
   */
  ResultVo<Boolean> callback(String body);

  class DefaultNullCallbackService implements AlgorithmCallbackService {

    @Override
    public CallBackAction getAction() {
      return CallBackAction.NULL;
    }

    @Override
    public ResultVo<Boolean> callback(String body) {
      throw new BusinessIllegalStateException("不支持的回调action");
    }
  }
}
