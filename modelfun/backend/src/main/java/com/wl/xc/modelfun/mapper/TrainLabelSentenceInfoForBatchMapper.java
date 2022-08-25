package com.wl.xc.modelfun.mapper;

import com.wl.xc.modelfun.entities.po.TrainLabelSentenceInfoPO;

/**
 * @version 1.0
 * @date 2022/6/9 19:20
 */
public interface TrainLabelSentenceInfoForBatchMapper {

  int insertSelective(TrainLabelSentenceInfoPO po);
}