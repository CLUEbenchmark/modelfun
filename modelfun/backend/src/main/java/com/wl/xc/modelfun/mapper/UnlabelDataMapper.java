package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.UnlabelDataPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 18:56
 */
public interface UnlabelDataMapper extends BaseMapper<UnlabelDataPO> {

  List<UnlabelDataPO> pageByTaskId(
      @Param("taskId") Long taskId, @Param("offset") Long offset, @Param("size") Integer size);

  int clearOldLabel(@Param("taskId") Long taskId);

  List<UnlabelDataPO> selectNoGptCache(Long taskId);

  Long getMaxIdByTask(Long taskId);

  int copyUnlabelDataFromTemplate(
      @Param("srcTask") Long srcTask,
      @Param("destTask") Long destTask,
      @Param("datasetId") Integer datasetId);
}
