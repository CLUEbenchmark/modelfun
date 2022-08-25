package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import java.util.List;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface DatasetDetailMapper extends BaseMapper<DatasetDetailPO> {

  int insertOrUpdate(DatasetDetailPO datasetDetailPO);

  List<DatasetDetailPO> getTemplateByTaskId(Long taskId);
}