package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.RuleType;
import lombok.Data;

/**
 * 规则描述对象，用于存放规则的描述信息，包括规则的元数据，规则的任务ID等
 *
 * @version 1.0
 * @date 2022.4.16 10:51
 */
@Data
public class RuleDescribe {

  /**
   * 规则的任务ID
   */
  private Long taskId;

  /**
   * 规则ID
   */
  private Long ruleId;

  /**
   * 规则类型
   */
  private RuleType ruleType;

  /**
   * 规则信息元数据
   */
  private String metadata;

  private Integer labelId;

}
