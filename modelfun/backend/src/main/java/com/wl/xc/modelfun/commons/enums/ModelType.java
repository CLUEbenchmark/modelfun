package com.wl.xc.modelfun.commons.enums;

import static java.util.stream.Collectors.toMap;

import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @date 2022/4/12 14:08
 */
@Getter
@AllArgsConstructor
public enum ModelType {

  LR(1, "传统模型", "lr"),
  BERT(2, "快速模型", "bert"),
  MAC_BERT(3, "标准模型", "macbert"),
  ERINE(4, "大模型", "erine"),
  ;

  private final int type;

  private final String name;

  private final String path;

  private static final Map<Integer, ModelType> MAP =
      Stream.of(ModelType.values()).collect(toMap(ModelType::getType, ModelType -> ModelType));

  public static ModelType getFromType(Integer type) {
    return Optional.ofNullable(type)
        .map(MAP::get)
        .orElseThrow(() -> new BusinessIllegalStateException("类型为" + type + "模型类型不存在"));
  }
}
