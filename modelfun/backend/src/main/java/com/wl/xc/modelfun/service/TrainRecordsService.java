package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.TrainRecordsPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.TrainRecordsMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class TrainRecordsService extends ServiceImpl<TrainRecordsMapper, TrainRecordsPO> {

  public PageVO<TrainRecordsPO> getTrainRecordsByTaskId(IPage<TrainRecordsPO> page, Long taskId) {
    page.orders().add(OrderItem.desc(TrainRecordsPO.COL_CREATE_DATETIME));
    IPage<TrainRecordsPO> result = baseMapper.selectPage(page, Wrappers.<TrainRecordsPO>query()
        .eq(TrainRecordsPO.COL_TASK_ID, taskId)
        .eq(TrainRecordsPO.COL_TRAIN_STATUS, 1));
    return PageUtil.convert(result);
  }

  public TrainRecordsPO getLastTrainRecord(Long taskId) {
    return baseMapper.getLastTrainRecord(taskId);
  }

  public TrainRecordsPO selectLatestDataVersion(Long taskId) {
    return baseMapper.selectLatestDataVersion(taskId);
  }

  public TrainRecordsPO getTemplateByTaskId(Long taskId) {
    return baseMapper.getTemplateByTaskId(taskId);
  }
}



