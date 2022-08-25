package com.wl.xc.modelfun.entities.model;

import com.wl.xc.modelfun.entities.dto.LabelDTO;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @version 1.0
 * @date 2022/6/2 14:55
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TextOneClickInput extends DatasetInput {

  private List<LabelDTO> labels;
}
