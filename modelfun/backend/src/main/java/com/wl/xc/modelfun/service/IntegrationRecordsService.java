package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.IntegrationRecordsPO;
import com.wl.xc.modelfun.mapper.IntegrationRecordsMapper;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class IntegrationRecordsService extends ServiceImpl<IntegrationRecordsMapper, IntegrationRecordsPO> {

  public IntegrationRecordsPO getLastIntegrationRecord(Long taskId) {
    return baseMapper.getLastIntegrationRecord(taskId);
  }

  public IntegrationRecordsPO getLastSuccessLabeledRecord(Long taskId) {
    return baseMapper.getLastSuccessLabeledRecord(taskId);
  }

  public IntegrationRecordsPO getTemplateByTaskId(Long taskId) {
    return baseMapper.getTemplateByTaskId(taskId);
  }
}



