package com.wl.xc.modelfun.entities.model;

import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.entities.po.TaskInfoPO;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/7/13 9:01
 */
@Data
public class InitTaskModel {


  private TaskInfoPO taskInfoPO;

  private DatasetInfoPO datasetInfoPO;
}
