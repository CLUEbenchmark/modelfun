package com.wl.xc.modelfun.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wl.xc.modelfun.entities.po.TrainResultPO;
import com.wl.xc.modelfun.entities.po.TrainResultWithRecordPO;
import com.wl.xc.modelfun.entities.vo.PageVO;
import com.wl.xc.modelfun.mapper.TrainResultMapper;
import com.wl.xc.modelfun.utils.PageUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @version 1.0
 * @date 2022/4/11 12:55
 */
@Service
public class TrainResultService extends ServiceImpl<TrainResultMapper, TrainResultPO> {


  public List<TrainResultPO> getByTaskIdAndTrainRecordId(Long taskId, List<Long> list) {
    return baseMapper.selectList(Wrappers.<TrainResultPO>query()
        .eq(TrainResultPO.COL_TASK_ID, taskId)
        .in(TrainResultPO.COL_TRAIN_RECORD_ID, list));
  }

  public PageVO<TrainResultWithRecordPO> pageByTaskId(IPage<TrainResultPO> page, Long taskId) {
    page.orders().add(OrderItem.desc(TrainResultPO.COL_CREATE_DATETIME));
    IPage<TrainResultWithRecordPO> trainResult = baseMapper.selectTrainResult(page, taskId);
    return PageUtil.convert(trainResult);
  }

  public TrainResultPO selectLatestOneTrainResult(Long taskId) {
    return baseMapper.selectLatestOneTrainResult(taskId);
  }

  public TrainResultWithRecordPO selectMatrixByTrainId(Long taskId, Long trainRecordId) {
    return baseMapper.selectMatrixByTrainId(taskId, trainRecordId);
  }

  public TrainResultPO getTemplateByTaskIdAndTrainId(Long taskId, Long trainRecordId) {
    return baseMapper.getTemplateByTaskIdAndTrainId(taskId, trainRecordId);
  }
}



