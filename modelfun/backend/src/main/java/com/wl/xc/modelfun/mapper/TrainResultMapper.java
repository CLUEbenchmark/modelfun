package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.po.TrainResultPO;
import com.wl.xc.modelfun.entities.po.TrainResultWithRecordPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface TrainResultMapper extends BaseMapper<TrainResultPO> {

  IPage<TrainResultWithRecordPO> selectTrainResult(IPage<TrainResultPO> page, @Param("taskId") Long taskId);

  TrainResultPO selectLatestOneTrainResult(@Param("taskId") Long taskId);

  TrainResultWithRecordPO selectMatrixByTrainId(@Param("taskId") Long taskId,
      @Param("trainRecordId") Long trainRecordId);

  TrainResultPO getTemplateByTaskIdAndTrainId(@Param("taskId") Long taskId, @Param("trainRecordId") Long trainRecordId);
}