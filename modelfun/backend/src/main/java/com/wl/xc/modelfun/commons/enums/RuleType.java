package com.wl.xc.modelfun.commons.enums;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标注规则类型枚举类
 *
 * @version 1.0
 * @date 2022/4/14 14:03
 */
@Getter
@AllArgsConstructor
public enum RuleType {

  FEW(-2, "文本小样本学习自动生成的规则"),
  REGEX(1, "模式匹配"),
  EXPERT(2, "专家知识"),
  DATABASE(3, "查找数据库"),
  OPEN_API(4, "外部系统"),
  LABEL_FUNCTION(5, "代码编写"),
  GPT3(6, "内置模型"),
  ;
  private final Integer type;

  private final String name;

  private static final Map<Integer, RuleType> MAP =
      Stream.of(RuleType.values())
          .collect(Collectors.toMap(RuleType::getType, Function.identity()));


  public static RuleType getByType(Integer ruleType) {
    return Optional.ofNullable(ruleType).map(MAP::get).orElse(null);
  }
}
