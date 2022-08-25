package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/4/20 21:32
 */
@NoArgsConstructor
@Data
public class RegexRule {

  /**
   * 规则类型。0：关键词，1：范围匹配，2：字符串长度，3：正则表达式
   */
  @JsonProperty("ruleType")
  private Integer ruleType;
  /**
   * 关键词
   */
  @JsonProperty("keyword")
  private String keyword;
  /**
   * 词频类型。1：==，2：!=，3：>=，4：<=，5：>，6：<
   */
  @JsonProperty("countType")
  private Integer countType;
  /**
   * 词频
   */
  private Integer count;
  /**
   * 结束关键词
   */
  @JsonProperty("endKeyword")
  private String endKeyword;

  /**
   * 间隔数量
   */
  private Integer gap;
  /**
   * 范围匹配间隔类型。0：向前间隔，1：间隔，2：向右无限间隔
   */
  @JsonProperty("gapType")
  private Integer gapType;
  /**
   * 关键词包含类型。0：包含，1：不包含，2：==
   */
  @JsonProperty("include")
  private Integer include;
  /**
   * 字符串长度类型。0：==，1：!=，2：>=，3：<=，4：>，5：<
   */
  @JsonProperty("lenType")
  private Integer lenType;
  /**
   * 字符串长度
   */
  @JsonProperty("len")
  private Integer len;
  /**
   * 正则表达式
   */
  @JsonProperty("regex")
  private String regex;
}
