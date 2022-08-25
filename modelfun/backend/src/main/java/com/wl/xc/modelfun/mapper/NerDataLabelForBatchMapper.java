package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.NerDataLabelPO;

/**
 * @version 1.0
 * @date 2022/5/24 15:02
 */
public interface NerDataLabelForBatchMapper extends BaseMapper<NerDataLabelPO> {

  int insertSelective(NerDataLabelPO nerDataLabelPO);
}