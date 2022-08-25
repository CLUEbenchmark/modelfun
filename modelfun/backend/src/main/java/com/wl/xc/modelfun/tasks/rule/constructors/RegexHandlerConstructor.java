package com.wl.xc.modelfun.tasks.rule.constructors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.model.RegexRule;
import com.wl.xc.modelfun.tasks.rule.RuleDescribe;
import com.wl.xc.modelfun.tasks.rule.RuleHandlerConstructor;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import com.wl.xc.modelfun.tasks.rule.handlers.RegexHandler;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022.4.16 11:04
 */
@Component
public class RegexHandlerConstructor implements RuleHandlerConstructor {

  private ObjectMapper objectMapper;

  @Override
  public RuleType getRuleType() {
    return RuleType.REGEX;
  }

  @Override
  public RuleTaskHandler createHandler(RuleDescribe ruleDescribe) {
    try {
      List<List<RegexRule>> lists = objectMapper.readValue(ruleDescribe.getMetadata(), new TypeReference<>() {
      });
      return new RegexHandler(lists, ruleDescribe.getLabelId());
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException(e);
    }
  }

  @Autowired
  public void ObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }
}
