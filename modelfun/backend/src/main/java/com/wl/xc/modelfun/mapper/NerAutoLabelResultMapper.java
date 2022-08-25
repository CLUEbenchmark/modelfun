package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.po.NerAutoLabelResultPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/5/25 18:37
 */
public interface NerAutoLabelResultMapper extends BaseMapper<NerAutoLabelResultPO> {

  IPage<NerAutoLabelResultPO> pageAutoLabelResult(
      IPage<NerAutoLabelResultPO> page,
      @Param("po") NerAutoLabelResultPO po,
      @Param("label") Integer label);

  int copyAutoLabelResultFromTemplate(@Param("srcTask") Long srcTask, @Param("destTask") Long destTask);
}
