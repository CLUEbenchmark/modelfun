package com.wl.xc.modelfun.commons.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @version 1.0
 * @date 2022/5/13 13:19
 */
public enum WsEventType {
  /**
   * 数据集相关事件
   */
  DATASET_PARSE_SUCCESS(1001, "dataset_parse", "数据集解析成功"),
  DATASET_PARSE_FAIL(1002, "dataset_parse", "数据集解析失败"),
  /**
   * 专家知识相关事件
   */
  EXPERT_PARSE_SUCCESS(2001, "expert_parse", "专家知识解析成功"),
  EXPERT_PARSE_FAIL(2002, "expert_parse", "专家知识解析失败"),
  /**
   * 规则运行相关事件
   */
  RULE_SUCCESS(3001, "rule", "规则运行成功"),
  RULE_FAIL(3002, "rule", "规则运行失败"),
  /**
   * 规则集成相关事件
   */
  INTEGRATED_SUCCESS(3001, "integrated", "规则集成成功"),
  INTEGRATED_FAIL(3002, "integrated", "规则集成失败"),
  /**
   * 自动标注相关事件
   */
  AUTO_LABEL_SUCCESS(4001, "auto_label", "自动标注成功"),
  AUTO_LABEL_FAIL(4002, "auto_label", "自动标注失败"),
  /**
   * 模型训练相关事件
   */
  TRAIN_SUCCESS(4001, "train", "模型训练成功"),
  TRAIN_FAIL(4002, "train", "模型训练失败"),
  /**
   * 一键标注相关事件
   */
  ONE_CLICK_SUCCESS(5001, "click", "一键标注成功"),
  ONE_CLICK_FAIL(5002, "click", "一键标注失败"),
  TEXT_CLICK_SUCCESS(5003, "text_click", "文本一键标注成功"),
  TEXT_CLICK_FAIL(5004, "text_click", "文本一键标注失败"),
  ;
  private final int code;

  private final String event;

  private final String desc;

  WsEventType(int code, String event, String desc) {
    this.code = code;
    this.event = event;
    this.desc = desc;
  }

  public int getCode() {
    return code;
  }

  @JsonValue
  public String getEvent() {
    return event;
  }

  public String getDesc() {
    return desc;
  }
}
