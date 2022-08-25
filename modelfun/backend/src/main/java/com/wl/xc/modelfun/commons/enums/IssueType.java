package com.wl.xc.modelfun.commons.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/7 17:23
 */
@Getter
@AllArgsConstructor
public enum IssueType {
  NOISE_DATA(1, "噪声数据"),
  AUTO_OPTIMIZATION_DATA(2, "自动优化数据"),
  ERROR_DATA(3, "错误数据"),
  INCONSISTENT_DATA(4, "不一致数据"),
  UNKNOWN_DATA(-1, "未知数据"),
  ;

  private static final Map<Integer, IssueType> MAP =
      Stream.of(IssueType.values())
          .collect(Collectors.toMap(IssueType::getType, Function.identity()));

  @JsonValue
  private final int type;

  private final String desc;

  @JsonCreator
  public static IssueType getFromType(int typeCode) {
    return Optional.ofNullable(MAP.get(typeCode)).orElse(UNKNOWN_DATA);
  }
}
