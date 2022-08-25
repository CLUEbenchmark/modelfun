package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.NerAutoLabelMapPO;
import com.wl.xc.modelfun.entities.po.NerDataLabelWithDesPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/5/25 18:37
 */
public interface NerAutoLabelMapMapper extends BaseMapper<NerAutoLabelMapPO> {

  List<NerDataLabelWithDesPO> getLabelResult(
      @Param("taskId") Long taskId,
      @Param("sentenceIds") List<Long> sentenceIds,
      @Param("dataType") Integer dataType);

  int copyDataFromTemplate(@Param("srcTask") Long srcTask, @Param("destTask") Long destTask);
}
