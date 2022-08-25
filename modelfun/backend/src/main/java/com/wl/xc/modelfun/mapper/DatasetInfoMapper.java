package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface DatasetInfoMapper extends BaseMapper<DatasetInfoPO> {

  DatasetInfoPO selectOneLastDatasetInfo(@Param("taskId") Long taskId);

  DatasetInfoPO getTemplateByTaskId(Long taskId);
}