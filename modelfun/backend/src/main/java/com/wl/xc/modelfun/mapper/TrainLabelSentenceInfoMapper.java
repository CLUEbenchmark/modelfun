package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.TrainLabelSentenceInfoPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/6/9 19:20
 */
public interface TrainLabelSentenceInfoMapper extends BaseMapper<TrainLabelSentenceInfoPO> {

  int copyDataFromTemplate(@Param("srcTrainId") Long srcTrainId, @Param("destTrainId") Long destTrainId);
}