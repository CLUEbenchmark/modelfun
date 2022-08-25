package com.wl.xc.modelfun.entities.dto.lfs;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.wl.xc.modelfun.commons.RuleChildTypeIdResolver;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/6/2 13:54
 */
@Data
@JsonTypeInfo(use = Id.CUSTOM, include = As.PROPERTY, property = "ruleType", visible = true)
@JsonTypeIdResolver(RuleChildTypeIdResolver.class)
public abstract class LabelRuleDTO<T> {

  private String ruleName;

  private Integer ruleType;

  private Integer label;

  private String labelDes;

  private T metadata;
}
