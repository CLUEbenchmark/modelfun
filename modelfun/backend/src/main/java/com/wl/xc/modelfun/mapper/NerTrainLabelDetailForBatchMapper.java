package com.wl.xc.modelfun.mapper;

import com.wl.xc.modelfun.entities.po.NerTrainLabelDetailPO;

/**
 * @version 1.0
 * @date 2022/6/9 18:08
 */
public interface NerTrainLabelDetailForBatchMapper {

  int insertSelective(NerTrainLabelDetailPO po);
}