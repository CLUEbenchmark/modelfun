package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface LabelInfoForBatchMapper extends BaseMapper<LabelInfoPO> {

  int insertSelective(LabelInfoPO labelInfoPO);
}