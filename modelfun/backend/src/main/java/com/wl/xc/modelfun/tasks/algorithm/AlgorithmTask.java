package com.wl.xc.modelfun.tasks.algorithm;

import com.wl.xc.modelfun.commons.enums.AlgorithmTaskType;
import java.util.Map;
import lombok.Data;

/**
 * 算法任务
 *
 * @version 1.0
 * @date 2022/4/20 10:46
 */
@Data
public class AlgorithmTask {

  private Long taskId;

  private AlgorithmTaskType type;

  private Long recordId;

  private String trainFileAddress;

  private Map<String, Object> params;

}
