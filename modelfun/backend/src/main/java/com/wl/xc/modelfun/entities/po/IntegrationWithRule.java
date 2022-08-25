package com.wl.xc.modelfun.entities.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @version 1.0
 * @date 2022/4/12 11:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IntegrationWithRule extends IntegrationResultPO {

  @TableField(value = "rule_name")
  private String ruleName;

}
