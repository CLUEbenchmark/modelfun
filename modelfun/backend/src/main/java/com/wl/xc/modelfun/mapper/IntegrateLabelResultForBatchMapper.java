package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.IntegrateLabelResultPO;

/**
 * @version 1.0
 * @date 2022/4/29 23:13
 */
public interface IntegrateLabelResultForBatchMapper extends BaseMapper<IntegrateLabelResultPO> {

  int insertSelective(IntegrateLabelResultPO po);
}