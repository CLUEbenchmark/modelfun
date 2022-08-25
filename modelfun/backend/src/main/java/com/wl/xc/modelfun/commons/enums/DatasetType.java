package com.wl.xc.modelfun.commons.enums;

import static com.wl.xc.modelfun.commons.FileConstant.LABEL_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.NER_LABEL_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.NER_TUNE_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.TEST_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.TRAIN_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.UN_LABEL_DATA_NAME;
import static com.wl.xc.modelfun.commons.FileConstant.VAL_DATA_NAME;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @version 1.0
 * @author: FanSJ
 * @date 2022/3/31 18:56
 */
public enum DatasetType {
  TEST(1, "测试集全集"),
  UNLABELED(2, "未标注数据集"),
  LABEL(3, "标签集"),
  TEST_SHOW(4, "验证集"),
  TEST_UN_SHOW(5, "测试集"),
  NER_LABEL(6, "NER标签集"),
  NER_TUNE(7, "NER微调标签示例"),
  TRAIN(8, "训练集"),
  NULL(-1, "无效数据集");;

  private static final Map<Integer, DatasetType> MAP =
      Stream.of(DatasetType.values())
          .collect(Collectors.toMap(DatasetType::getType, Function.identity()));

  private final Integer type;

  private final String name;

  DatasetType(Integer type, String name) {
    this.type = type;
    this.name = name;
  }

  @JsonValue
  public Integer getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  @JsonCreator
  public static DatasetType jacksonCreator(String type) {
    int value;
    try {
      value = Integer.parseInt(type);
    } catch (NumberFormatException e) {
      return null;
    }
    DatasetType datasetType = getFromType(value);
    return datasetType == NULL ? null : datasetType;
  }

  public static DatasetType getFromType(Integer type) {
    return Optional.ofNullable(MAP.get(type)).orElse(NULL);
  }

  public static DatasetType getFromName(String name) {
    if (name == null) {
      return NULL;
    }
    if (name.toLowerCase().contains(TEST_DATA_NAME)) {
      return TEST_UN_SHOW;
    } else if (name.toLowerCase().contains(VAL_DATA_NAME)) {
      return TEST_SHOW;
    } else if (name.toLowerCase().contains(UN_LABEL_DATA_NAME)) {
      return UNLABELED;
    } else if (name.toLowerCase().contains(LABEL_DATA_NAME)) {
      return LABEL;
    } else if (name.toLowerCase().contains(NER_LABEL_NAME)) {
      return NER_LABEL;
    } else if (name.toLowerCase().contains(NER_TUNE_NAME)) {
      return NER_TUNE;
    } else if (name.toLowerCase().contains(TRAIN_DATA_NAME)) {
      return TRAIN;
    } else {
      return NULL;
    }
  }

  @Override
  public String toString() {
    return "DatasetType{" + "type=" + type + ", name='" + name + '\'' + '}';
  }
}
