package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.TestDataPO;

/**
 * @version 1.0
 * @date 2022/4/11 18:56
 */
public interface TestDataForBatchMapper extends BaseMapper<TestDataPO> {

  int insertSelective(TestDataPO entity);
}