package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.NerAutoLabelTrainPO;
import com.wl.xc.modelfun.mapper.NerAutoLabelTrainMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/6/14 15:54
 */
@Service
public class NerAutoLabelTrainService
    extends ServiceImpl<NerAutoLabelTrainMapper, NerAutoLabelTrainPO> {

  public void deleteByTaskId(Long taskId) {
    baseMapper.delete(
        Wrappers.<NerAutoLabelTrainPO>query().eq(NerAutoLabelTrainPO.COL_TASK_ID, taskId));
  }

  public List<NerAutoLabelTrainPO> selectByTaskId(Long taskId) {
    return baseMapper.selectList(
        Wrappers.<NerAutoLabelTrainPO>query().eq(NerAutoLabelTrainPO.COL_TASK_ID, taskId));
  }

  public int copyDataFromTemplate(Long srcTask, Long destTask) {
    return baseMapper.copyDataFromTemplate(srcTask, destTask);
  }
}
