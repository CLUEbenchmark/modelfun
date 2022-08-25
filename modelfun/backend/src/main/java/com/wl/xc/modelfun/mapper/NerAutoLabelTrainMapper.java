package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.NerAutoLabelTrainPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/6/14 15:54
 */
public interface NerAutoLabelTrainMapper extends BaseMapper<NerAutoLabelTrainPO> {

  int copyDataFromTemplate(@Param("srcTask") Long srcTask, @Param("destTask") Long destTask);
}