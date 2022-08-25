package com.wl.xc.modelfun.commons.enums;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @version 1.0
 * @date 2022/6/8 9:39
 */
public enum AutoLabelType {

  CORRECT(1, "正确"),
  DOUBTFUL(2, "存疑"),
  ;

  private static final Map<Integer, AutoLabelType> MAP =
      Stream.of(AutoLabelType.values())
          .collect(Collectors.toMap(AutoLabelType::getType, Function.identity()));

  private final int type;

  private final String desc;

  AutoLabelType(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  public int getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }

  public static AutoLabelType getFromType(Integer type) {
    return Optional.ofNullable(MAP.get(type)).orElseThrow(() -> new BusinessIllegalStateException("错误的数据类型"));
  }
}
