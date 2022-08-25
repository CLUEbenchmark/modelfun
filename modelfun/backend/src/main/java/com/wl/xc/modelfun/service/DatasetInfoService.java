package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.DatasetInfoPO;
import com.wl.xc.modelfun.mapper.DatasetInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class DatasetInfoService extends ServiceImpl<DatasetInfoMapper, DatasetInfoPO> {

  public DatasetInfoPO getLastDatasetInfo(Long taskId) {
    return baseMapper.selectOneLastDatasetInfo(taskId);
  }

  public int removeByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<DatasetInfoPO>query().eq(DatasetInfoPO.COL_TASK_ID, taskId));
  }

  public DatasetInfoPO getTemplateByTaskId(Long taskId) {
    return baseMapper.getTemplateByTaskId(taskId);
  }
}
