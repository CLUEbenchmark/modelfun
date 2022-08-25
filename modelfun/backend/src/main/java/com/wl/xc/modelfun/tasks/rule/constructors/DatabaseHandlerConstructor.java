package com.wl.xc.modelfun.tasks.rule.constructors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wl.xc.modelfun.commons.enums.RuleType;
import com.wl.xc.modelfun.commons.exceptions.BusinessIllegalStateException;
import com.wl.xc.modelfun.entities.model.DatabaseRule;
import com.wl.xc.modelfun.service.LabelInfoService;
import com.wl.xc.modelfun.tasks.rule.RuleDescribe;
import com.wl.xc.modelfun.tasks.rule.RuleHandlerConstructor;
import com.wl.xc.modelfun.tasks.rule.RuleTaskHandler;
import com.wl.xc.modelfun.tasks.rule.handlers.DatabaseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @version 1.0
 * @date 2022.4.16 11:10
 */
@Component
public class DatabaseHandlerConstructor implements RuleHandlerConstructor {

  private ObjectMapper objectMapper;

  private LabelInfoService labelInfoService;

  @Override
  public RuleType getRuleType() {
    return RuleType.DATABASE;
  }

  @Override
  public RuleTaskHandler createHandler(RuleDescribe ruleDescribe) {
    String metadata = ruleDescribe.getMetadata();
    DatabaseRule databaseRule;
    try {
      databaseRule = objectMapper.readValue(metadata, DatabaseRule.class);
    } catch (JsonProcessingException e) {
      throw new BusinessIllegalStateException("错误的规则内容，转换为数据库规则失败", e);
    }
    DatabaseHandler handler = new DatabaseHandler(databaseRule, ruleDescribe.getTaskId());
    handler.setLabelInfoService(labelInfoService);
    return handler;
  }

  @Autowired
  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Autowired
  public void setLabelInfoService(LabelInfoService labelInfoService) {
    this.labelInfoService = labelInfoService;
  }
}
