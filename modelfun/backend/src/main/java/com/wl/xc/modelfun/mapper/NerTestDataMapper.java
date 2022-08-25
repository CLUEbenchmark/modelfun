package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.po.NerTestDataPO;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/5/24 14:48
 */
public interface NerTestDataMapper extends BaseMapper<NerTestDataPO> {

  IPage<NerTestDataPO> pageNerTestData(
      IPage<NerTestDataPO> page,
      @Param("po") NerTestDataPO po,
      @Param("label") Integer label,
      @Param("isDesc") boolean isDesc);

  Long selectMaxSentenceId(@Param("taskId") Long taskId, @Param("dataType") Integer dataType);

  int countUnLabeledData(@Param("taskId") Long taskId, @Param("dataType") int dataType);

  int copyDataFromTemplate(@Param("srcTask") Long srcTask, @Param("destTask") Long destTask);
}
