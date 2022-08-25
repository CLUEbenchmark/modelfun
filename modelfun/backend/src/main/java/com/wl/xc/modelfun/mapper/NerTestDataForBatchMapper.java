package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.NerTestDataPO;

/**
 * @version 1.0
 * @date 2022/5/24 14:48
 */
public interface NerTestDataForBatchMapper extends BaseMapper<NerTestDataPO> {

  int insertSelective(NerTestDataPO nerTestDataPO);
}