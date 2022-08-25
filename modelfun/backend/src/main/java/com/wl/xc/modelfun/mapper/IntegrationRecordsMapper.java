package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface IntegrationRecordsMapper extends BaseMapper<IntegrationRecordsPO> {

  IntegrationRecordsPO getLastIntegrationRecord(@Param("taskId") Long taskId);

  IntegrationRecordsPO getLastSuccessLabeledRecord(@Param("taskId") Long taskId);

  IntegrationRecordsPO getTemplateByTaskId(Long taskId);
}