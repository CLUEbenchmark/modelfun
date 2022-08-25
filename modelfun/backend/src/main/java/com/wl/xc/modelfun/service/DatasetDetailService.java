package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.DatasetDetailPO;
import com.wl.xc.modelfun.mapper.DatasetDetailMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class DatasetDetailService extends ServiceImpl<DatasetDetailMapper, DatasetDetailPO> {

  public int insertOrUpdate(DatasetDetailPO datasetDetailPO) {
    return baseMapper.insertOrUpdate(datasetDetailPO);
  }

  public DatasetDetailPO selectByTaskIdAndType(Long taskId, Integer type) {
    return baseMapper.selectOne(
        Wrappers.<DatasetDetailPO>query()
            .eq(DatasetDetailPO.COL_TASK_ID, taskId)
            .eq(DatasetDetailPO.COL_FILE_TYPE, type));
  }

  public long countByTaskId(Long taskId) {
    return baseMapper.selectCount(Wrappers.<DatasetDetailPO>query().eq(DatasetDetailPO.COL_TASK_ID, taskId));
  }

  public int removeByTaskId(Long taskId) {
    return baseMapper.delete(Wrappers.<DatasetDetailPO>query().eq(DatasetDetailPO.COL_TASK_ID, taskId));
  }

  public List<DatasetDetailPO> getTemplateByTaskId(Long taskId) {
    return baseMapper.getTemplateByTaskId(taskId);
  }
}
