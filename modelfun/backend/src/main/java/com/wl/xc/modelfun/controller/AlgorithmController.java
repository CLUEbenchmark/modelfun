package com.wl.xc.modelfun.controller;

import com.wl.xc.modelfun.commons.enums.CallBackAction;
import com.wl.xc.modelfun.commons.enums.ResponseCodeEnum;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.components.AlgorithmCallbackFactory;
import com.wl.xc.modelfun.entities.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 与算法交互的接口
 *
 * @version 1.0
 * @date 2022/6/28 13:43
 */
@Slf4j
@RestController
@RequestMapping("/algorithm")
public class AlgorithmController {

  private AlgorithmCallbackFactory callbackFactory;

  /**
   * <pre>
   * 算法回调通用接口，用于接收算法的异步回调。
   * 目前使用String类型作为接收参数，因为历史原因以及算法返回的内容格式不确定，不太方便使用对象接收，只能使用String接收，
   * 然后根据action类型解析为对应的回调对象。
   * </pre>
   *
   * @param body   回调请求体
   * @param action 回调动作类型
   * @return 是否成功
   */
  @RequestMapping("/callback")
  public ResultVo<Boolean> callback(
      @RequestBody String body, @RequestParam(name = "action") Integer action) {
    CallBackAction actionType = CallBackAction.getByType(action);
    if (CallBackAction.NULL.equals(actionType)) {
      log.error("[AlgorithmController.callback] 算法回调的action参数不合法，action={}", action);
      return ResultVo.create(ResponseCodeEnum.ILLEGAL_PARAMETER, false, false);
    }
    log.info("[AlgorithmController.callback] 收到算法回调，任务类型={}", actionType.getDesc());
    try {
      return callbackFactory.getService(actionType).callback(body);
    } catch (BusinessException e) {
      log.error("[AlgorithmController.callback] 算法回调处理失败，任务类型={}", actionType.getDesc(), e);
      return ResultVo.create(e.getMessage(), -1, false, false);
    } catch (Exception e) {
      log.error("[AlgorithmController.callback]", e);
      return ResultVo.create(ResponseCodeEnum.INTERNAL_EXCEPTION, false, false);
    }
  }

  @Autowired
  public void setCallbackFactory(AlgorithmCallbackFactory callbackFactory) {
    this.callbackFactory = callbackFactory;
  }
}
