package com.wl.xc.modelfun.commons.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/8 11:24
 */
@Getter
@AllArgsConstructor
public enum OpType {
  ROLLBACK(0, "回滚"),
  UPDATE(1, "修改"),
  DEL(2, "删除"),
  ADD(3, "新增"),
  NULL(-1, "未知类型"),
  ;

  private final int type;

  private final String desc;

  private static final Map<Integer, OpType> MAP =
      Stream.of(OpType.values()).collect(toMap(OpType::getType, opType -> opType));

  public static OpType getFromType(Integer type) {
    return Optional.ofNullable(type).map(MAP::get).orElse(NULL);
  }
}
