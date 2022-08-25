package com.wl.xc.modelfun.entities.model;

import java.util.regex.Pattern;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/22 11:12
 */
@Data
public class RegexRuleWithPattern {

  private RegexRule regexRule;

  private Pattern pattern;
}
