package com.wl.xc.modelfun.entities.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version 1.0
 * @date 2022/6/6 14:34
 */
@NoArgsConstructor
@Data
public class RegexRule4Data {

  /**
   * 规则类型。0：关键词，1：范围匹配，2：字符串长度，3：正则表达式
   */
  @JsonProperty("ruleType")
  private String ruleType;
  /**
   * 关键词
   */
  @JsonProperty("keyword")
  private String keyword;
  /**
   * 词频类型。1：==，2：!=，3：>=，4：<=，5：>，6：<
   */
  @JsonProperty("countType")
  private String countType;
  /**
   * 词频
   */
  private String count;
  /**
   * 结束关键词
   */
  @JsonProperty("endKeyword")
  private String endKeyword;

  /**
   * 间隔数量
   */
  private String gap;
  /**
   * 范围匹配间隔类型。0：向前间隔，1：间隔，2：向右无限间隔
   */
  @JsonProperty("gapType")
  private String gapType;
  /**
   * 关键词包含类型。0：包含，1：不包含，2：==
   */
  @JsonProperty("include")
  private String include;
  /**
   * 字符串长度类型。0：==，1：!=，2：>=，3：<=，4：>，5：<
   */
  @JsonProperty("lenType")
  private String lenType;
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

  public RegexRule4Data(RegexRule regexRule) {
    this.ruleType = Optional.ofNullable(regexRule.getRuleType()).map(Object::toString).orElse(null);
    this.keyword = regexRule.getKeyword();
    this.countType = Optional.ofNullable(regexRule.getCountType()).map(Object::toString).orElse(null);
    this.count = Optional.ofNullable(regexRule.getCount()).map(Object::toString).orElse(null);
    this.endKeyword = regexRule.getEndKeyword();
    this.gap = Optional.ofNullable(regexRule.getGap()).map(Object::toString).orElse(null);
    this.gapType = Optional.ofNullable(regexRule.getGapType()).map(Object::toString).orElse(null);
    this.include = Optional.ofNullable(regexRule.getInclude()).map(Object::toString).orElse(null);
    this.lenType = Optional.ofNullable(regexRule.getLenType()).map(Object::toString).orElse(null);
    this.len = regexRule.getLen();
    this.regex = regexRule.getRegex();
  }
}
