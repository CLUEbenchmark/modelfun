package com.wl.xc.modelfun.commons.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @date 2022/4/20 10:48
 */
@AllArgsConstructor
@Getter
public enum AlgorithmTaskType {
  INTEGRATION(1, "任务集成"),

  AUTO_LABEL(2, "自动标注"),
  MODEL_TRAIN(3, "模型训练"),
  NER_ONE_CLICK(4, "NER一键标注"),
  TEXT_ONE_CLICK(5, "文本一键标注"),
  NER_AUTO_LABEL(6, "NER自动标注"),
  NER_TRAIN(7, "NER模型训练"),
  FEW_SHOT(8, "文本小样本学习"),
  NULL(-1, "不参与任务"),
  ;

  private final Integer type;

  private final String name;
}
