package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 18:56
 */
public interface UnlabelDataForBatchMapper extends BaseMapper<UnlabelDataPO> {

  int insertSelective(UnlabelDataPO unlabelDataPO);

  int updateByPrimaryKeySelective(UnlabelDataPO unlabelDataPO);

  List<UnlabelDataPO> listByDataId(
      @Param("taskId") Long taskId, @Param("sentenceIds") Collection<Long> sentenceIds);
}
