package com.wl.xc.modelfun.tasks.rule.handlers;

import com.wl.xc.modelfun.commons.enums.DatasetType;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.entities.model.RegexRule;
import com.wl.xc.modelfun.entities.model.RegexRuleWithPattern;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * 模式匹配的规则处理类，改类用于处理模式匹配的规则
 *
 * <p>该模式下需要一组正则表达式以及一个标签ID，当输入的语料匹配到任一正则表达式时，则该语料对应的标签ID就是该规则的标签ID
 *
 * @version 1.0
 * @date 2022/4/15 13:30
 */
public class RegexHandler implements RuleTaskHandler {

  private List<RulePattern> patternList;

  private final List<List<RegexRule>> regexRuleLists;

  private final Integer labelId;

  public RegexHandler(List<List<RegexRule>> regexRuleLists, Integer labelId) {
    this.regexRuleLists = regexRuleLists;
    this.labelId = labelId;
  }

  @Override
  public RuleType getRuleType() {
    return RuleType.REGEX;
  }

  @Override
  public void init() {
    List<RulePattern> p = new ArrayList<>(regexRuleLists.size());
    for (List<RegexRule> ruleList : regexRuleLists) {
      List<RegexRuleWithPattern> list = new ArrayList<>(ruleList.size());
      for (RegexRule regexRule : ruleList) {
        RegexRuleWithPattern withPattern = new RegexRuleWithPattern();
        withPattern.setRegexRule(regexRule);
        if (regexRule.getRuleType() != 0) {
          withPattern.setPattern(Pattern.compile(regexRule.getRegex()));
        }
        list.add(withPattern);
      }
      RulePattern pattern = new RulePattern();
      pattern.rulePatterns = list;
      p.add(pattern);
    }
    patternList = p;
  }

  @Override
  public int label(String sentence, DatasetType datasetType) {
    for (RulePattern pattern : patternList) {
      if (pattern.match(sentence)) {
        return labelId;
      }
    }
    return -1;
  }

  @Override
  public void afterLabel() {

  }

  @Override
  public void destroy() {
  }

  static class RulePattern {

    List<RegexRuleWithPattern> rulePatterns;

    /**
     * 匹配语料，多个规则是与的关系
     *
     * @param sentence 语料
     * @return 是否匹配
     */
    public boolean match(String sentence) {
      for (RegexRuleWithPattern rulePattern : rulePatterns) {
        RegexRule regexRule = rulePattern.getRegexRule();
        Integer ruleType = regexRule.getRuleType();
        if (ruleType != 0) {
          Pattern pattern = rulePattern.getPattern();
          if (!pattern.matcher(sentence).find()) {
            return false;
          }
        } else {
          Integer include = regexRule.getInclude();
          String keyword = regexRule.getKeyword();
          if (include == 1) {
            // 语料中不包含关键词
            if (sentence.contains(keyword)) {
              return false;
            }
          } else if (include == 2) {
            // 语料中==关键词
            if (!sentence.equals(keyword)) {
              return false;
            }
          } else if (include == 0) {
            // 语料中包含关键词，并且有词频设置
            Integer countType = regexRule.getCountType();
            Integer count = regexRule.getCount();
            if (count == null) {
              countType = 0;
            }
            if (countType == 0) {
              // 词频设置为0，语料中包含关键词
              if (keyword.isBlank() || !sentence.contains(keyword)) {
                return false;
              }
            } else {
              int countMatches = StringUtils.countMatches(sentence, keyword);
              // 词频设置为1，语料中包含关键词count次
              if (countType == 1 && countMatches != count) {
                // 语料中包含关键词count次
                return false;
              } else if (countType == 2) {
                // 语料中出现关键词次数 != count
                if (countMatches == count) {
                  return false;
                }
              } else if (countType == 3) {
                // 语料中出现关键词次数 >= count
                if (countMatches < count) {
                  return false;
                }
              } else if (countType == 4) {
                // 语料中出现关键词次数 <= count
                if (countMatches > count) {
                  return false;
                }
              } else if (countType == 5) {
                // 语料中出现关键词次数 > count
                if (countMatches <= count) {
                  return false;
                }
              } else if (countType == 6) {
                // 语料中出现关键词次数 < count
                if (countMatches >= count) {
                  return false;
                }
              } else {
                return false;
              }
            }
          } else {
            return false;
          }
        }
      }
      return true;
    }
  }

  /*static class RegexType {
    private Integer ruleType;
    private Integer includeType;
    private
  }*/
}
