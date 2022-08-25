package com.wl.xc.modelfun.entities.model;

import com.wl.xc.modelfun.entities.po.TaskExpertPO;
import java.util.List;
import lombok.Data;

/**
 * @version 1.0
 * @date 2022/4/15 11:13
 */
@Data
public class ExpertParseModel {

  List<FileUpload> uploadList;

  List<TaskExpertPO> expertPOList;
}
