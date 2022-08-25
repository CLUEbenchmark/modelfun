package com.wl.xc.modelfun.tasks.rule;

import com.wl.xc.modelfun.commons.enums.RuleTaskType;
import com.wl.xc.modelfun.entities.po.RuleInfoPO;
import java.util.List;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022.4.16 12:03
 */
@Data
public class RuleTask {

  private Long taskId;

  private RuleTaskType type;

  private RuleInfoPO ruleInfo;

  private List<RuleInfoPO> ruleInfoPOList;

  private RuleTaskConfig config = new RuleTaskConfig();

}
