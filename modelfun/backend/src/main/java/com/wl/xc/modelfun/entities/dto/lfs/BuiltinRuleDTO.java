package com.wl.xc.modelfun.entities.dto.lfs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.wl.xc.modelfun.entities.model.BuiltinModelRule;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/6/2 14:26
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(value = Include.NON_NULL)
public class BuiltinRuleDTO extends LabelRuleDTO<BuiltinModelRule> {

}