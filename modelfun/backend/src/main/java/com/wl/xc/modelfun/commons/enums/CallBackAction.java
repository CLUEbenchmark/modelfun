package com.wl.xc.modelfun.commons.enums;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 算法回调类型
 *
 * @version 1.0
 * @date 2022/6/28 14:26
 */
public enum CallBackAction {
  INTEGRATION(1, "任务集成"),
  TEXT_AUTO_LABEL(2, "文本自动标注"),
  TEXT_MODEL_TRAIN(3, "文本模型训练"),
  NER_ONE_CLICK(4, "NER一键标注"),
  NER_AUTO_LABEL(5, "NER自动标注"),
  NER_TRAIN(6, "NER模型训练"),
  TEXT_FEW_SHOT(7, "文本小样本学习"),
  BUILTIN_MODEL(8, "内置模型"),
  NULL(-1, "未知类型"),
  ;

  private static final Map<Integer, CallBackAction> MAP =
      Stream.of(CallBackAction.values())
          .collect(Collectors.toMap(CallBackAction::getType, Function.identity()));

  private final int type;

  private final String desc;

  CallBackAction(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public int getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }

  public static CallBackAction getByType(Integer type) {
    return Optional.ofNullable(type).map(MAP::get).orElse(NULL);
  }
}
