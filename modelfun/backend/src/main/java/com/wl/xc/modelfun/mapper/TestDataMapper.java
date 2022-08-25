package com.wl.xc.modelfun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.xc.modelfun.entities.po.TestDataPO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @version 1.0
 * @date 2022/4/11 18:56
 */
public interface TestDataMapper extends BaseMapper<TestDataPO> {

  Long groupCountLabelByTaskId(@Param("taskId") Long taskId);

  List<TestDataPO> selectNoGptCache(@Param("taskId") Long taskId, @Param("show") int show);

  int insertAndAutoIncrement(TestDataPO po);

  List<TestDataPO> pageTestData(
      @Param("taskId") Long taskId,
      @Param("dataType") Integer dataType,
      @Param("offset") long offset,
      @Param("size") int size);

  List<TestDataPO> getTrainCorrectData(
      @Param("taskId") Long taskId, @Param("trainRecordId") Long trainRecordId);

  int copyTestDataFromTemplate(
      @Param("src") Long srcTaskId,
      @Param("dest") Long destTaskId,
      @Param("datasetId") Integer datasetId);
}
