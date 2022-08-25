package com.wl.xc.modelfun.entities.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/5/26 16:56
 */
@Data
public class OneClickEntityDTO {

  private String text;

  private List<?> relations;

  private Map<String, Map<String, List<List<Integer>>>> label;
}
