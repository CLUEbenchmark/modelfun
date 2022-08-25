package com.wl.xc.modelfun.tasks.algorithm.handlers;

import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import com.wl.xc.modelfun.commons.exceptions.BusinessException;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.tasks.algorithm.AlgorithmTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022/4/20 11:45
 */
@Slf4j
@Component
public class ModelTrainSubHandler extends ModelTrainHandler {

  @Override
  public AlgorithmTaskType getType() {
    return AlgorithmTaskType.NULL;
  }

  @Override
  protected void handleOnError(AlgorithmTask task, Exception exception) {
    if (exception instanceof BusinessException) {
      throw new BusinessIllegalStateException(String.format("模型训练失败，错误信息：%s", exception.getMessage()),
          exception);
    } else {
      throw new BusinessIllegalStateException(String.format("模型训练失败，错误信息：%s！", "系统内部错误，请联系管理员"),
          exception);
    }
  }
}
