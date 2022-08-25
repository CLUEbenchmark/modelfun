package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wl.xc.modelfun.entities.po.AutoLabelResultPO;
import com.wl.xc.modelfun.entities.po.IntegrateLabelResultPO;
import com.wl.xc.modelfun.entities.po.SimpleAutoLabelResult;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/29 23:13
 */
public interface IntegrateLabelResultMapper extends BaseMapper<IntegrateLabelResultPO> {

  IPage<IntegrateLabelResultPO> selectPageByTaskIdAndKeyword(
      IPage<?> page, @Param("po") IntegrateLabelResultPO po);

  List<AutoLabelResultPO> pageLabelCorrectByTaskId(
      @Param("taskId") long taskId, @Param("offset") long offset, @Param("size") int size);

  List<SimpleAutoLabelResult> pageCorrectByTaskId(
      @Param("taskId") long taskId, @Param("offset") long offset, @Param("size") int size);

  int countLabelByTaskId(Long taskId);

  int copyAutoLabelResultFromTemplate(
      @Param("srcTask") Long srcTask,
      @Param("destTask") Long destTask,
      @Param("datasetId") Integer datasetId);
}
