package com.wl.xc.modelfun.commons.enums;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 文件任务的类型
 *
 * @version 1.0
 * @author: FanSJ
 * @date 2022/4/1 16:14
 */
public enum FileTaskType {
  DATASET(1, "数据集"),
  EXPERT(2, "专家知识"),
  OSS_DEL(3, "删除OSS文件"),
  HF_WORD(4, "测试集文件高频词汇解析"),
  NER(5, "NER一键标注类型任务的数据集文件"),
  NER_R(6, "NER任务的数据集文件"),
  NULL(-1, "未知类型"),
  ;
  private final int type;

  private final String desc;

  private static final Map<Integer, FileTaskType> MAP =
      Stream.of(FileTaskType.values()).collect(toMap(FileTaskType::getType, e -> e));

  FileTaskType(int type, String desc) {
    this.type = type;
    this.desc = desc;
  }

  /**
   * 根据任务类型的int值获取对应的枚举
   *
   * @param type 任务类型的int值
   * @return 对应的枚举。如果找不到对应的枚举，则返回{@link #NULL}
   */
  public static FileTaskType getByType(Integer type) {
    return Optional.ofNullable(type).map(MAP::get).orElse(NULL);
  }

  public int getType() {
    return type;
  }

  public String getDesc() {
    return desc;
  }
}
