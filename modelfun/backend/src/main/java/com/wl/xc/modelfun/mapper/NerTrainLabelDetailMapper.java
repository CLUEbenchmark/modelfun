package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.NerTrainLabelDetailPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/6/9 18:08
 */
public interface NerTrainLabelDetailMapper extends BaseMapper<NerTrainLabelDetailPO> {

  int copyDataFromTemplate(@Param("srcTrainId") Long srcTrainId, @Param("destTrainId") Long destTrainId);
}