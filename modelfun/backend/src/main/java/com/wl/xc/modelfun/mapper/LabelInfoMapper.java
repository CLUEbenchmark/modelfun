package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.po.LabelInfoPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 16:13
 */
public interface LabelInfoMapper extends BaseMapper<LabelInfoPO> {

  IPage<LabelInfoPO> pageLabelInfo(IPage<LabelInfoPO> page, @Param("po") LabelInfoPO labelInfoPO);

  int copyLabelInfoFromTemplate(
      @Param("srcTask") Long srcTask,
      @Param("destTask") Long destTask,
      @Param("datasetId") Integer datasetId);
}
