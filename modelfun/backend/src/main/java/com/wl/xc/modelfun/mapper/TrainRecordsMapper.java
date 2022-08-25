package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.TrainRecordsPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface TrainRecordsMapper extends BaseMapper<TrainRecordsPO> {

  TrainRecordsPO getLastTrainRecord(@Param("taskId") Long taskId);

  TrainRecordsPO selectLatestDataVersion(@Param("taskId") Long taskId);

  TrainRecordsPO getTemplateByTaskId(Long taskId);
}