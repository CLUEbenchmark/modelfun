package com.wl.xc.modelfun.tasks.algorithm;

import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;

/**
 * 算法相关的任务处理器
 *
 * @version 1.0
 * @date 2022/4/20 11:40
 */
public interface AlgorithmHandler {

  AlgorithmTaskType getType();

  void handle(AlgorithmTask task);

}
