package com.wl.xc.modelfun.commons.enums;

import static java.util.stream.Collectors.toMap;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @version 1.0
 * @date 2022/5/18 13:51
 */
public enum BuiltinModelType {
  GPT3(1, "gpt3"),
  SIM(2, "sim"),
  ROBERTA(3, "roberta"),
  CLUSTERING(4, "clustering"),
  ;

  private final int type;

  private final String name;

  private static final Map<Integer, BuiltinModelType> MAP =
      Stream.of(BuiltinModelType.values()).collect(toMap(BuiltinModelType::getType, t -> t));

  BuiltinModelType(int type, String name) {
    this.type = type;
    this.name = name;
  }

  public int getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public static BuiltinModelType getFromType(Integer type) {
    return Optional.ofNullable(type)
        .map(MAP::get)
        .orElseThrow(() -> new BusinessIllegalStateException("类型为" + type + "模型类型不存在"));
  }
}
